package com.taobao.tmallsearch.common.utils.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.io.IOUtils;

import com.taobao.tmallsearch.httpasync.client.HttpAsyncClient;
import com.taobao.tmallsearch.httpasync.client.impl.HttpAsyncClientImpl;
import com.tmall.search.httpclient.client.HttpRequest;
import com.tmall.search.httpclient.client.HttpResponse;
import com.tmall.search.httpclient.client.TmallHttpClient;

public class Benchmark {

	public static void main(String[] args) throws Exception {
		//NabaHttp();
		tmallHttp();
		//ApacheHttp();
		//Thread.sleep(10000);
	}

	public static void tmallHttp() throws Exception {
		TmallHttpClient tmallHttpClient = new TmallHttpClient();
		ExecutorService ex = Executors.newFixedThreadPool(10);
		long st = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			Thread a = new Thread(new Tmallexecute(i, tmallHttpClient));
			a.setName("Thread" + i);
			ex.execute(a);
		}
		ex.shutdown();
		try {
			while (!ex.awaitTermination(10, TimeUnit.MILLISECONDS)) {

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis() - st);
		tmallHttpClient.close();
	}

	public static class Tmallexecute implements Runnable {
		int i;
		TmallHttpClient tmallHttpClient;

		public Tmallexecute(int i, TmallHttpClient tmallHttpClient) {
			this.i = i;
			this.tmallHttpClient = tmallHttpClient;
		}

		@Override
		public void run() {
			for (int j = 3000; j < 5000; j++) {
				HttpRequest req;
				try {
					//req = new HttpRequest("http://10.232.43.8:8000/qp?s=relasearchmall&c=2&src=tmall-search_10.72.87.152&k=nike");
					req = new HttpRequest("http://localhost:8080/BenchmadeWeb/xxx?q="+j);
					HttpResponse hr = tmallHttpClient.executeMethod(req);
					//System.out.println(i +"-" +j);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	//-----------------------------------------------------------------------------------------------------------
	public static void ApacheHttp() throws Exception {
		HttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams hmp = new HttpConnectionManagerParams();
		hmp.setSoTimeout(10000);
		manager.setParams(hmp);
		HttpClient apacheHttpClient = new HttpClient(manager);
		ExecutorService ex = Executors.newFixedThreadPool(10);
		long st = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			Thread a = new Thread(new Apacheexecute(i, apacheHttpClient));
			a.setName("Thread" + i);
			ex.execute(a);
		}
		ex.shutdown();
		try {
			while (!ex.awaitTermination(10, TimeUnit.MILLISECONDS)) {

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis() - st);
	}

	public static class Apacheexecute implements Runnable {
		int i;
		HttpClient apachelHttpClient;

		public Apacheexecute(int i, HttpClient apachelHttpClient) {
			this.i = i;
			this.apachelHttpClient = apachelHttpClient;
		}

		@Override
		public void run() {
			for (int j = 3000; j < 5000; j++) {
				HttpRequest req;
				try {
					HttpMethod method = new GetMethod("http://localhost:8080/BenchmadeWeb/xxx?q=" + j);
					//HttpMethod method = new GetMethod("http://10.232.43.8:8000/qp?s=relasearchmall&c=2&src=tmall-search_10.72.87.152&k=nike");
					try {
						apachelHttpClient.executeMethod(method);
					} catch (Exception e) {
						method.releaseConnection();
						e.printStackTrace();
					}

					byte[] responseBody = null;
					try {
						InputStream is = method.getResponseBodyAsStream();
						responseBody = IOUtils.toByteArray(is);
						is.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					method.releaseConnection();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	//-----------------------------------------------------------------------------------------------------------
	public static void NabaHttp() throws Exception {
		System.out.println(System.currentTimeMillis());
		HttpAsyncClient client = new HttpAsyncClientImpl();
		client.start();
		ExecutorService ex = Executors.newFixedThreadPool(10);
		long st = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			Thread a = new Thread(new Nabaexecute(i, client));
			a.setName("Thread" + i);
			ex.execute(a);
		}
		ex.shutdown();
		try {
			while (!ex.awaitTermination(5, TimeUnit.MILLISECONDS)) {

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis() - st);
	}

	public static class Nabaexecute implements Runnable {
		int i;
		HttpAsyncClient nabaHttpClient;

		public Nabaexecute(int i, HttpAsyncClient nabaHttpClient) {
			this.i = i;
			this.nabaHttpClient = nabaHttpClient;
		}

		@Override
		public void run() {
			for (int j =3000; j < 5000; j++) {
				com.taobao.tmallsearch.httpasync.client.entity.HttpRequest request = new com.taobao.tmallsearch.httpasync.client.entity.HttpRequest("http://localhost:8080/BenchmadeWeb/xxx?q=" + j);
				//com.taobao.tmallsearch.httpasync.client.entity.HttpRequest request = new com.taobao.tmallsearch.httpasync.client.entity.HttpRequest("http://10.232.43.8:8000/qp?s=relasearchmall&c=2&src=tmall-search_10.72.87.152&k=nike");

				Future<com.taobao.tmallsearch.httpasync.client.entity.HttpResponse> future = nabaHttpClient.execute(request, null);
				com.taobao.tmallsearch.httpasync.client.entity.HttpResponse response;
				try {
					response = future.get();
					byte[] bytes = response.getBytesContent();
					//System.out.println(j + "\t" +new String(bytes));
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println(System.currentTimeMillis());
		}
	}
}