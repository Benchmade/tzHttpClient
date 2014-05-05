package com.tmall.search.httpclient.conn;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.tmall.search.httpclient.util.HttpException;

public interface HttpConnection {
	/**
	 * 发送请求获得数据.
	 * @param method
	 * @return
	 * @throws Exception
	 */
	public void sendRequest(byte[] requestData) throws HttpException;

	public void close() throws IOException;

	/**
	 * read once
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public ByteBuffer read() throws HttpException;

	/**
	 * 连接是否过期.
	 * @return 返回true 没过期 ,返回false 连接过期
	 */
	public boolean isExpired();
	
	public long getLastUseTime();
	
	/**
	 * 指定过期时间删除过期Conn
	 * @param idletime
	 * @param tunit
	 * @return
	 */
	public boolean isExpired(long idletime, TimeUnit tunit);
	
	public String getRemoteAddress();
	
}
