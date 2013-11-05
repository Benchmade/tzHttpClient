package com.tmall.search.httpclient.conn;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.tmall.search.httpclient.params.ConnManagerParams;
import com.tmall.search.httpclient.util.HttpException;

public interface HttpConnectiongManager {
	public HttpConnection getConnectionWithTimeout(HttpHost host) throws HttpException ,IOException ;
	public void freeConnection(HttpHost host , HttpConnection conn);
	public void deleteConnection(HttpHost host, HttpConnection conn) throws IOException;
	public void shutDown() throws IOException;
	public void closeIdleConnections(long idletime, TimeUnit tunit);
	public ConnManagerParams getParam();
}
