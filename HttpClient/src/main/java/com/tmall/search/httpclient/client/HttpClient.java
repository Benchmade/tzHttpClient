package com.tmall.search.httpclient.client;

import java.io.IOException;

import com.tmall.search.httpclient.conn.HttpConnectiongManager;
import com.tmall.search.httpclient.conn.ThreadSafeConnectionManager;
import com.tmall.search.httpclient.util.HttpException;


public class HttpClient {

	private HttpConnectiongManager connManager;
	
	public HttpClient() {
		this.connManager = new ThreadSafeConnectionManager();
	}
	
	public HttpClient(HttpConnectiongManager connManager) {
		this.connManager = connManager;
	}
	
	public HttpResponse executeMethod(HttpRequest req) throws HttpException, IOException{
		RequestDirector director = new RequestDirector(connManager,req);
		return director.execute();
	}
	
	public void close() throws IOException {
		connManager.shutDown();
	}
	
}
