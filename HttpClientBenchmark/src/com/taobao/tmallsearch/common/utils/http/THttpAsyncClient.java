package com.taobao.tmallsearch.common.utils.http;

import java.util.concurrent.Future;

import com.taobao.common.searchengine.apache.HttpClientFactory;
import com.taobao.common.searchengine.apache.HttpClientWapper;
import com.taobao.tmallsearch.httpasync.client.HttpAsyncClient;
import com.taobao.tmallsearch.httpasync.client.entity.HttpRequest;
import com.taobao.tmallsearch.httpasync.client.entity.HttpResponse;
import com.taobao.tmallsearch.httpasync.client.impl.HttpAsyncClientImpl;
import com.taobao.tmallsearch.httpasync.concurrent.FutureCallback;
import com.taobao.tmallsearch.httpasync.reactor.IOReactorException;

public class THttpAsyncClient {
    public static HttpAsyncClient client = null;

    private final static String testUrl = "http://list.daily.tmall.net//search_product.htm?tbpm=1&q=nike";

    static {
        try {
            client = new HttpAsyncClientImpl();
            client.start();
        } catch (IOReactorException e) {
            System.out.println(e);
        }
    }

    public void testSync() throws Exception {
    	 long s = System.currentTimeMillis();
    	for (int i = 0; i < 1000; i++) {
            HttpRequest request = new HttpRequest(testUrl+"1");
            Future<HttpResponse> future = client.execute(request, null);
            HttpResponse response = future.get();
            byte[] bytes = response.getBytesContent();
        }
    	 System.out.println(System.currentTimeMillis() - s);
    }

    public void testApacheHttpClient() throws Exception {
    	 long s = System.currentTimeMillis();
    	for (int i = 0; i < 100; i++) {
            String queryURL = testUrl;
            HttpClientWapper httpclient = HttpClientFactory.getHttpClient();
            System.out.println(new String(httpclient.getBytes(queryURL)));
        }
    	 System.out.println(System.currentTimeMillis() - s);
    }

    public void testAsync() throws IOReactorException {
        for (int i = 0; i < 100; i++) {
            String queryURL = testUrl;
            HttpRequest request = new HttpRequest(queryURL);
            client.execute(request, new FutureCallback<HttpResponse>() {
                @Override
                public void failed(Exception ex) {
                    System.out.println(ex);
                }

                @Override
                public void completed(HttpResponse result) {
                    try {
                    	byte[] xx = result.getBytesContent();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void cancelled() {
                    System.out.println("cancelled");
                }
            });
        }
    }

    public static void main(String[] args) throws Exception {
    	THttpAsyncClient test = new THttpAsyncClient();
        test.testApacheHttpClient();
    }
}
