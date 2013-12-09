package com.tmall.search.httpclient.conn;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.tmall.search.httpclient.params.ConnManagerParams;
import com.tmall.search.httpclient.util.HttpException;

/**
 * 
 * @author xiaolin.mxl
 */
public interface HttpConnectiongManager {
	/**
	 * 获得Conn对象
	 * @param host	target server address
	 * @return
	 * @throws HttpException
	 */
	public HttpConnection getConnectionWithTimeout(HttpHost host) throws HttpException,IOException;
	
	/**
	 * 释放conn
	 * @param host
	 * @param conn
	 */
	public void freeConnection(HttpHost host , HttpConnection conn);
	
	/**
	 * delete expired conn
	 * @param host
	 * @param conn
	 * @throws HttpException
	 */
	public void deleteConnection(HttpHost host, HttpConnection conn) throws HttpException;
	public void shutDown() throws IOException;
	/**
	 * @return clear idle conn num
	 * @throws IOException
	 */
	public int closeIdleConnections(long idletime, TimeUnit tunit) throws IOException;
	public ConnManagerParams getParam();
}
