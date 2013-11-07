package com.tmall.search.httpclient.conn;

import java.nio.ByteBuffer;

import com.tmall.search.httpclient.client.Header;
import com.tmall.search.httpclient.util.HttpException;

public interface ContentPaser {

	public byte[] paser() throws HttpException;
	
	public void reset(HttpConnection conn, Header header, ByteBuffer buffer);

}
