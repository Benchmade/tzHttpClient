package com.tmall.search.httpclient.client;

import java.io.IOException;

import com.tmall.search.httpclient.conn.HttpConnectiongManager;
import com.tmall.search.httpclient.conn.ThreadSafeConnectionManager;
import com.tmall.search.httpclient.util.HttpException;


public class TmallHttpClient {

	HttpConnectiongManager connManager;
	
	public TmallHttpClient() {
		this.connManager = new ThreadSafeConnectionManager();
	}
	
	public TmallHttpClient(HttpConnectiongManager connManager) {
		this.connManager = connManager;
	}
	
	public HttpResponse executeMethod(HttpRequest req) throws HttpException, IOException{
		RequestDirector director = new RequestDirector(connManager,req);
		return director.execute();
	}
	
	
	
	public void close() throws IOException {
		connManager.shutDown();
	}
	 
	public static void main(String[] args) throws Exception{//2644  4604
		//Thread.sleep(10000);
		TmallHttpClient h = new TmallHttpClient();
		long s = System.currentTimeMillis();
		for(int i=0;i<10000;i++){
			HttpRequest req = new HttpRequest("http://localhost:8080/BenchmadeWeb/xxx?q="+i);
			HttpResponse hr = h.executeMethod(req);
			System.out.println(i);
			//System.out.println(hr.toString());
		}
		System.out.println(System.currentTimeMillis()-s);
		h.close();
		
		
	}
	
}
