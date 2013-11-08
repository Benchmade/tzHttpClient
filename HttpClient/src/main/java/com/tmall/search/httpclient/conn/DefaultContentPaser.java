package com.tmall.search.httpclient.conn;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.tmall.search.httpclient.client.Header;
import com.tmall.search.httpclient.util.ByteUtil;
import com.tmall.search.httpclient.util.HttpException;

public class DefaultContentPaser implements ContentPaser {

	private HttpConnection conn;
	private ByteBuffer readBuffer;
	private boolean done;
	private Header header;

	public DefaultContentPaser(HttpConnection conn, Header header, ByteBuffer readBuffer) {
		this.conn = conn;
		this.readBuffer = readBuffer;
		this.header = header;
	}

	@Override
	public void reset(HttpConnection conn, Header header, ByteBuffer buffer) {
		this.conn = conn;
		this.readBuffer = buffer;
		this.header = header;
		this.done = false;
	}

	/**
	 * 强依赖于Content-Length,可能会有问题. 依赖buffer.limit() == buffer.capacity()这个可能长度正好等于capacity,
	 * buffer.limit() == buffer.capacity() || sy > 0 时会进入循环.
	 * buffer.limit() == buffer.capacity() && sy > 0 可能第一次读取没有读取满buffer,但是Content-Length>0
	 */
	@Override
	public byte[] paser() throws HttpException {
		if (done) {
			throw new HttpException("finished reading the buffer ,Please Invoke reset() method");
		}
		if (header.getHeaderElements().get(Header.CONTENT_LEN) == null) {
			throw new HttpException("Header must contains Content-Length");
		}
		ByteBuffer buffer = readBuffer;
		byte[] respData = ByteUtil.mergeByteArray(null, buffer.array(), header.getLength(), buffer.limit() - header.getLength());
		int remainingLength = 0;
		remainingLength = Integer.parseInt(header.getHeaderElements().get(Header.CONTENT_LEN)) - (buffer.limit() - header.getLength());
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
		done = true;
		return respData;
	}
}