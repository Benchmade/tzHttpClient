package com.tmall.search.httpclient.conn;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.tmall.search.httpclient.client.Header;
import com.tmall.search.httpclient.util.ByteUtil;
import com.tmall.search.httpclient.util.HttpException;

public class DefaultContentPaser implements ContentPaser {

	/**
	 * 强依赖于Content-Length,可能会有问题. 依赖buffer.limit() == buffer.capacity()这个可能长度正好等于capacity,
	 * buffer.limit() == buffer.capacity() || sy > 0 时会进入循环.
	 * buffer.limit() == buffer.capacity() && sy > 0 可能第一次读取没有读取满buffer,但是Content-Length>0
	 */
	@Override
	public byte[] paser(HttpConnection conn, Header header, ByteBuffer readBuffer) throws HttpException {
		ByteBuffer buffer = readBuffer;
		byte[] respData = ByteUtil.mergeByteArray(null, buffer.array(), header.getLength(), buffer.limit() - header.getLength());
		int remainingLength = Integer.parseInt(header.getHeaderElements().get("Content-Length")) - (buffer.limit() - header.getLength());
		buffer.clear();
		while (remainingLength > 0) { // buffer.limit() == buffer.capacity() || sy > 0 如果读取长度和容量一样,可能没有读取完,需要再次读取
			try {
				buffer = conn.read();
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new HttpException("Read Server request error", e);
			}
			remainingLength = remainingLength - buffer.limit();
			respData = ByteUtil.mergeByteArray(respData, buffer.array(), buffer.limit());
			buffer.clear();
		}
		return respData;
	}
}
