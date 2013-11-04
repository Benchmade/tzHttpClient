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
		for(int i=0;i<100;i++){
			HttpRequest req = new HttpRequest("http://list.daily.tmall.net//search_product.htm?tbpm=1&q=nike");
			//HttpRequest req = new HttpRequest("http://10.232.43.8:8000/qp?usernid=-1&rqtest=dynamic&s=mall&c=50024400&src=tmall-search_10.13.134.15&k=&rc=50024400&nopt=1&outfmt=xml");
			HttpResponse hr = h.executeMethod(req);
			System.out.println(hr.toString());
			/*if(i%100==0){
				System.out.println(hr.toString());
			}*/
			//System.out.println(i);
			//Thread.sleep(20000);
		}
		System.out.println(System.currentTimeMillis()-s);
		h.close();
		
		
	}
	
}
