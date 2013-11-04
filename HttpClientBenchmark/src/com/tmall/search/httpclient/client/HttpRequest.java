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
		public String toString() {
			return this.name().toUpperCase();
		}
	}

	private URI uriInfo;
	private byte[] sendData;
	private HttpHost host;
	private boolean followRedirects = true;
	private String cookie = null;
	private boolean enableCompress = false;

	public HttpRequest(String url) throws URISyntaxException, UnsupportedEncodingException {
		this(url, MethodName.GET);
	}

	public HttpRequest(String url, MethodName methodName) throws URISyntaxException, UnsupportedEncodingException {
		uriInfo = new URI(url);
		host = new HttpHost(uriInfo.getHost(), uriInfo.getPort());
		StringBuilder sb = new StringBuilder();
		writeRequestLine(sb,methodName);
		
		sb.append("Host: ").append(uriInfo.getHost());
		if (uriInfo.getPort() != -1) {
			sb.append(":");
			sb.append(uriInfo.getPort());
		}
		sb.append(HttpUtil.CRLF);
		
		if (enableCompress) {
			sb.append("Accept-Encoding: gzip,deflate").append(HttpUtil.CRLF);
		}
		
		if (cookie != null && cookie.trim().length() > 0) {
			sb.append("Cookie: ").append(cookie).append(HttpUtil.CRLF);
		}
		sb.append(HttpUtil.CRLF);
		sendData = sb.toString().getBytes("US-ASCII");
	}

	private void writeRequestLine(StringBuilder sb, MethodName methodName) {
		String reuqestMethod = methodName.toString();
		sb.append(reuqestMethod).append(" ");//GET 
		sb.append(uriInfo.getPath());
		if (uriInfo.getRawQuery() != null) {
			sb.append("?");
			sb.append(uriInfo.getRawQuery());
		}
		sb.append(" HTTP/1.1").append(HttpUtil.CRLF);
	}


	public byte[] getSendData() {
		return sendData;
	}

	public HttpHost getHost() {
		return host;
	}

	public boolean isFollowRedirects() {
		return followRedirects;
	}

	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public static void main(String[] args) throws Exception {
		HttpRequest m = new HttpRequest("http://www.amazon.cn/b/ref=sa_menu_softwa_l3_b811142051?ie=UTF8&node=811142051");
		System.out.println(new String(m.getSendData()));
	}

}
