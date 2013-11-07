package com.tmall.search.httpclient.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import com.tmall.search.httpclient.client.Header;

public final class ByteUtil {

	private static final byte[] CHUNK_END = new byte[] { 10, 13, 10, 13, 48 };

	/**
	 * 合并2个byteArray,读取数据时,可能一个bytebuffer不能完全读取完,使用这个方法,合并多次读取到的byte
	 * @param data			原有数组
	 * @param increment		新增数组
	 * @param length		新增数组长度
	 * @return 				组合后的新数组
	 */
	public static byte[] mergeByteArray(byte[] data, byte[] increment, int length) {
		return mergeByteArray(data, increment, 0, length);
	}

	/**
	 * 合并2个byteArray,读取数据时,可能一个bytebuffer不能完全读取完,使用这个方法,合并多次读取到的byte
	 * @param data			原有数组
	 * @param increment		新增数组
	 * @param copyLength		copy长度
	 * @return 				组合后的新数组
	 */
	public static byte[] mergeByteArray(byte[] data, byte[] increment, int start, int copyLength) {
		byte[] resultArray;
		if (data == null || data.length == 0) {//如果原始的数组是空的,那么直接截取新增的数组
			if (increment == null || copyLength <= 0 || start == increment.length) {
				resultArray = new byte[0];
			} else {
				resultArray = new byte[copyLength];
				System.arraycopy(increment, start, resultArray, 0, copyLength);
			}
		} else {
			if (increment == null || copyLength <= 0 || start == increment.length) { //如果后面的数组限定条件有问题,那么直接返回前面的数组
				resultArray = data;
			} else {
				resultArray = new byte[data.length + copyLength];
				System.arraycopy(data, 0, resultArray, 0, data.length);
				System.arraycopy(increment, start, resultArray, data.length, copyLength);
			}
		}
		return resultArray;
	}

	@Deprecated
	public static boolean isChunkEnd(ChunkStateInfo chunkInfo, byte[] data, int length) {
		if (data == null || length == 0) {
			return true;
		}
		if (chunkInfo.getLastBuffRemaining() != null && chunkInfo.getLastBuffRemaining().length + length < 5) {
			throw new NullPointerException("Check chunk end mark error");
		}
		byte cc;
		for (int i = 0; i < CHUNK_END.length; i++) {
			if (length - i - 1 < 0) {
				cc = chunkInfo.getLastBuffRemaining()[chunkInfo.getLastBuffRemaining().length + length - i - 1];
			} else {
				cc = data[length - i - 1];
			}
			if (CHUNK_END[i] != cc) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param chunkInfo	chunkContext的信息.
	 * @param buffer	当前读取出来的buffer
	 * @param pos	buffer其实位置.
	 * @param length	buffer的长度
	 * @return
	 */
	@Deprecated
	public static int fillChunkBody(ChunkStateInfo chunkInfo, byte[] buffer, int pos, int length) {
		if (chunkInfo.getLastBuffRemaining() != null) {//如果上次有没有读取完的剩余byte[],直接合并到当前的byte[]中.
			buffer = mergeByteArray(chunkInfo.getLastBuffRemaining(), buffer, pos, length - pos);
			pos = 0;
			length = buffer.length;//长度设置为合并后的byte[]长度
			chunkInfo.setLastBuffRemaining(null);
		}
		if (chunkInfo.getUnFinishedNum() > 0) {//如果上次读取到结尾,没有读取到结束符      1,2,3  -  13,10这次应该校验,代码没有写
			pos = pos + chunkInfo.getUnFinishedNum();
			chunkInfo.setUnFinishedNum(0);
		}
		if (chunkInfo.getShengyu() > 0) {//如果上次有没有读取完的,这次继续读取
			if (chunkInfo.getShengyu() > length - pos) {//如果剩余的大小大于这次读取的内容,那么直接把这次的内容都读取进来
				chunkInfo.setChunkData(mergeByteArray(chunkInfo.getChunkData(), buffer, pos, length - pos));
				chunkInfo.setShengyu(chunkInfo.getShengyu() - (length - pos));
				pos = length;
			} else {
				chunkInfo.setChunkData(mergeByteArray(chunkInfo.getChunkData(), buffer, pos, chunkInfo.getShengyu()));
				pos = pos + chunkInfo.getShengyu();
				chunkInfo.setShengyu(0);
				if (length - pos < 2) {
					chunkInfo.setUnFinishedNum(2 - (length - pos));
					pos = length;
				} else {
					if (buffer[pos] != Header.CR && buffer[pos + 1] != Header.LF) {
						throw new NullPointerException("this chunk terminated abnormally CRLF");//没有正常结束
					} else {
						pos = pos + 2;
						if (length - pos < 2) {
							byte[] residueData = new byte[length - pos];
							System.arraycopy(buffer, pos, residueData, 0, length - pos);
							chunkInfo.setLastBuffRemaining(residueData);
							pos = length;
						}
					}
				}
			}
		}
		for (int i = pos; i < length - 1; i++) {
			if (buffer[i] == Header.CR && buffer[i + 1] == Header.LF) {
				int chunkSize = Integer.parseInt(new String(buffer, pos, i - pos), 16);
				if (chunkSize == 0) {
					if (i + 3 < length) {
						break;
					}
				} else {
					chunkInfo.setShengyu(chunkSize);
					i = fillChunkBody(chunkInfo, buffer, i + 2, length);
					pos = i;
				}
			}
			if (i == length - 2) {//如果读取到最后还没有匹配CRLF,那么数据规整,返回到下次读取时整合到下次读取的byte中.
				byte[] residueData = new byte[length - pos];
				System.arraycopy(buffer, pos, residueData, 0, length - pos);
				chunkInfo.setLastBuffRemaining(residueData);
			}
		}
		return pos;
	}

	
	/**
	 * http gzip 解压缩
	 * @param compressData	原始数据
	 * @return 解压后byteArray
	 * @throws HttpException
	 */
	public static byte[] unCompress(byte[] compressData) throws HttpException{
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressData);
		GZIPInputStream gZIPInputStream = null;
		byte[] result = null;
		try {
			gZIPInputStream = new GZIPInputStream(byteArrayInputStream);
			result = IOUtils.toByteArray(gZIPInputStream);
			byteArrayInputStream.close();
		} catch (IOException e) {
			try {
				byteArrayInputStream.close();
				if(gZIPInputStream!=null){
					gZIPInputStream.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			throw new HttpException("UnCompress Failure.",e);
		}
		return result;
	}
}
