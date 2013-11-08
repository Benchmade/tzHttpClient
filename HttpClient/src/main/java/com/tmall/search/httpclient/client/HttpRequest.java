package com.tmall.search.httpclient.client;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.tmall.search.httpclient.compress.AcceptDecoder;
import com.tmall.search.httpclient.conn.HttpHost;
import com.tmall.search.httpclient.params.RequestParams;

/**
 * httprequest请求数据描述类,没有写param类定义可配置参数.
 * @author xiaolin.mxl
 *
 */
public class HttpRequest {
	
	/**
	 * @author xiaolin.mxl
	 */
	public static enum MethodName {
		GET, POST;
		@Override
		public String toString() {
			return this.name().toUpperCase();
		}
	}
	
	/**
	 * 协议版本
	 * @author xiaolin.mxl
	 */
	public static enum ProtocolVersion {
		HTTP11("HTTP/1.1"),
		HTTP10("HTTP/1.0");
		
		private String version;
		private ProtocolVersion(String version) {
			this.version = version;
		}
		public String getVersion() {
			return this.version;
		}
	}

	private URI uriInfo;
	private byte[] sendData;
	private HttpHost host;
	private String cookieValue = null;
	//默认设置全局固定参数,调用set方法,覆盖全局设置.
	private boolean compress = RequestParams.enableCompass;
	private boolean followRedirects = RequestParams.enableFollowRedirects;
	private ProtocolVersion protocolVersion = RequestParams.protocolVersion;
	private List<AcceptDecoder> inOrderAcceptEncodingList = RequestParams.inOrderAcceptEncodingList;
	
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
		sb.append(Header.CRLF);
		
		if (compress) {
			sb.append("Accept-Encoding: gzip").append(Header.CRLF);
		}
		
		if (cookieValue != null && cookieValue.trim().length() > 0) {
			sb.append("Cookie: ").append(cookieValue).append(Header.CRLF);
		}
		sb.append(Header.CRLF);
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
		sb.append(" ").append(protocolVersion.version).append(Header.CRLF);
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
	public String getCookieValue() {
		return cookieValue;
	}
	public void setCookieValue(String cookieValue) {
		this.cookieValue = cookieValue;
	}
	public boolean isCompress() {
		return compress;
	}
	public void setCompress(boolean compress) {
		this.compress = compress;
	}
	
	public ProtocolVersion getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(ProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public List<AcceptDecoder> getInOrderAcceptEncodingList() {
		return inOrderAcceptEncodingList;
	}

	public void setInOrderAcceptEncodingList(List<AcceptDecoder> inOrderAcceptEncodingList) {
		this.inOrderAcceptEncodingList = inOrderAcceptEncodingList;
	}

	public static void main(String[] args) throws Exception {
		HttpRequest m = new HttpRequest("http://www.amazon.cn/b/ref=sa_menu_softwa_l3_b811142051?ie=UTF8&node=811142051");
		System.out.println(new String(m.getSendData()));
	}

}
