package com.tmall.search.httpclient.conn;

import java.nio.ByteBuffer;

import com.tmall.search.httpclient.client.Header;
import com.tmall.search.httpclient.util.HttpException;

public interface ContentPaser {

	public byte[] paser(HttpConnection conn, Header header, ByteBuffer buffer) throws HttpException;

}
