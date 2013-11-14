package com.tmall.search.httpclient.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
	 
	public static void main(String[] args) throws Exception{//2644  4604
		//Thread.sleep(10000);
		HttpClient h = new HttpClient();
		long s = System.currentTimeMillis();
		FileOutputStream fos = new FileOutputStream(new File("d:/xxx.txt"));
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw);
		for(int i=3000;i<10000;i++){
			HttpRequest req = new HttpRequest("http://localhost:8080/BenchmadeWeb/xxx?q="+i);
			HttpResponse hr = h.executeMethod(req);
			bw.write(hr.toString());
			bw.newLine();
			
		}
		bw.close();
		osw.close();
		fos.close();
		System.out.println(System.currentTimeMillis()-s);
		h.close();
		
		
	}
	
}
