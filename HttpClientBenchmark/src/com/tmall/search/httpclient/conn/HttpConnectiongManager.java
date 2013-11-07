package com.tmall.search.httpclient.conn;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.tmall.search.httpclient.params.ConnManagerParams;
import com.tmall.search.httpclient.util.HttpException;

public interface HttpConnectiongManager {
	public HttpConnection getConnectionWithTimeout(HttpHost host) throws HttpException ;
	public void freeConnection(HttpHost host , HttpConnection conn);
	public void deleteConnection(HttpHost host, HttpConnection conn) throws HttpException;
	public void shutDown() throws IOException;
	public void closeIdleConnections(long idletime, TimeUnit tunit) throws IOException;
	public ConnManagerParams getParam();
}
