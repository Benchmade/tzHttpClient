package com.tmall.search.httpclient.util;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import com.tmall.search.httpclient.client.Header;
import com.tmall.search.httpclient.client.HttpRequest;

public final class ByteUtil {
	
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

	public static byte[] assemblyRequestBody(String requestLine, Map<String,String> headerElements) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder(8);
		sb.append(requestLine);
		for(Entry<String,String> entry: headerElements.entrySet()){
			sb.append(entry.getKey()).append(HttpRequest.COLON).append(entry.getValue()).append(Header.CRLF);
		}
		sb.append(Header.CRLF);
		return sb.toString().getBytes("US-ASCII");
	}

}
