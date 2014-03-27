package com.tmall.search.httpclient.client;

import java.io.IOException;

import com.tmall.search.httpclient.conn.HttpConnectiongManager;
import com.tmall.search.httpclient.conn.ThreadSafeConnectionManager;
import com.tmall.search.httpclient.params.ConnManagerParams;
import com.tmall.search.httpclient.util.HttpException;

/**
 * @author xiaolin.mxl
 */
public class HttpClient {

	private HttpConnectiongManager connManager;
	private ConnManagerParams connManagerParams;
	
	public HttpClient() {
		this.connManager = new ThreadSafeConnectionManager(connManagerParams);
	}
	
	public HttpClient(ConnManagerParams connManagerParams) {
		if(connManagerParams==null){
			connManagerParams = new ConnManagerParams();
		}
		this.connManagerParams = connManagerParams;
		this.connManager = new ThreadSafeConnectionManager(connManagerParams);
	}
	
	public ConnManagerParams getConnManagerParams() {
		return connManagerParams;
	}

	public HttpResponse executeMethod(HttpRequest req) throws HttpException, IOException{
		RequestDirector director = new RequestDirector(connManager,req);
		return director.execute();
	}
	
	public HttpResponse execute(String url)throws HttpException, IOException{
		RequestDirector director = new RequestDirector(connManager,new HttpRequest(url));
		return director.execute();
	}
	
	public void close() throws IOException,HttpException {
		connManager.shutDown();
	}
	
}
