package com.tmall.search.httpclient.conn;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.tmall.search.httpclient.client.HttpRequest;
import com.tmall.search.httpclient.client.HttpResponse;
import com.tmall.search.httpclient.util.ProtocolException;

public interface HttpConnection {

	/**
	 * 发送请求获得数据.
	 * @param method
	 * @return
	 * @throws Exception
	 */
	public void sendRequest(HttpRequest method) throws InterruptedException, ExecutionException, TimeoutException;

	public HttpResponse readResp() throws InterruptedException, ExecutionException, TimeoutException, ProtocolException;

	public void close() throws IOException;

	public ByteBuffer read() throws InterruptedException, ExecutionException, TimeoutException;

	/**
	 * 连接是否过期.
	 * @return 返回true 没过期 ,返回false 连接过期
	 */
	public boolean isExpired();
	
	public boolean isExpired(long idletime, TimeUnit tunit);

}
