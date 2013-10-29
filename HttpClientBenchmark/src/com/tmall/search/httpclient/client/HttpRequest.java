package com.tmall.search.httpclient.client;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import com.tmall.search.httpclient.conn.HttpHost;
import com.tmall.search.httpclient.util.HttpUtil;

/**
 * httprequest请求数据描述类
 * @author xiaolin.mxl
 *
 */
public class HttpRequest {
	public static enum MethodName {
		GET, POST;
		@Override
		public String toString(){
			return this.name().toUpperCase();
		}
	}

	private URI uriInfo;
	private String reuqestMethod;
	private byte[] sendData;
	private HttpHost host;
	public HttpRequest(String url) throws URISyntaxException, UnsupportedEncodingException {
		this(url, MethodName.GET, "US-ASCII");
	}

	public HttpRequest(String url,String charset) throws URISyntaxException, UnsupportedEncodingException {
		this(url, MethodName.GET,charset);
	}
	
	public HttpRequest(String url,MethodName methodName) throws URISyntaxException, UnsupportedEncodingException {
		this(url, methodName, "US-ASCII");
	}
	
	public HttpRequest(String url, MethodName methodName,String charset) throws URISyntaxException, UnsupportedEncodingException{
		uriInfo = new URI(url);
		host = new HttpHost(uriInfo.getHost(), uriInfo.getPort());
		reuqestMethod = methodName.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(reuqestMethod);
		sb.append(" ");
		sb.append(uriInfo.getPath());
		if(uriInfo.getRawQuery()!=null){
			sb.append("?");
			sb.append(uriInfo.getRawQuery());
		}
		sb.append(" HTTP/1.1").append(HttpUtil.CRLF);
		sb.append("Host: ");
		sb.append(uriInfo.getHost());
		if(uriInfo.getPort()!=-1){
			sb.append(":");
			sb.append(uriInfo.getPort());
		}
		sb.append(HttpUtil.CRLF);
		sb.append(HttpUtil.CRLF);
		sendData = sb.toString().getBytes(charset);
	}

	public String getMethod(){
		return reuqestMethod;
	}
	
	public byte[] getSendData() {
		return sendData;
	}

	public HttpHost getHost() {
		return host;
	}

	public static void main(String[] args) throws Exception {
		HttpRequest m = new HttpRequest("http://list.daily.tmall.net/search_product.htm?q=nike");
		System.out.println(new String(m.getSendData()));
	}

}
