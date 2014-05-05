package com.tmall.search.httpclient.conn;

import java.nio.ByteBuffer;

import com.tmall.search.httpclient.client.Header;
import com.tmall.search.httpclient.util.ByteUtils;
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

	@Override
	public byte[] paser() throws HttpException {
		/*if(header.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY){
			return new byte[0];
		}*/
		if (done) {
			throw new HttpException("finished reading the buffer ,Please Invoke reset() method");
		}
		if (header.getHeaderElement(Header.CONTENT_LEN) == null) {
			throw new HttpException("Header must contains Content-Length!\n" + new String(readBuffer.array()));
		}
		ByteBuffer buffer = readBuffer;
		byte[] respData = ByteUtils.mergeByteArray(null, buffer.array(), header.getLength(), buffer.limit() - header.getLength());
		int remainingLength = 0;
		remainingLength = Integer.parseInt(header.getHeaderElement(Header.CONTENT_LEN)) - (buffer.limit() - header.getLength());
		buffer.clear();
		while (remainingLength > 0) {
			buffer = conn.read();
			remainingLength = remainingLength - buffer.limit();
			respData = ByteUtils.mergeByteArray(respData, buffer.array(), buffer.limit());
			buffer.clear();
		}
		done = true;
		return respData;
	}

}
