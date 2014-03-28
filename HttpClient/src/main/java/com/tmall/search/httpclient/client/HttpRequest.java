package com.tmall.search.httpclient.client;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tmall.search.httpclient.conn.HttpHost;
import com.tmall.search.httpclient.params.RequestParams;

/**
 * httprequest请求数据描述类,没有写param类定义可配置参数.
 * @author xiaolin.mxl
 */
public class HttpRequest {

	public static final String ACCEPT_ENCODING = "Accept-Encoding";
	public static final String HOST = "Host";
	public static final String COLON = ":";

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
		HTTP11("HTTP/1.1"), HTTP10("HTTP/1.0");

		private String version;

		private ProtocolVersion(String version) {
			this.version = version;
		}

		public String getVersion() {
			return this.version;
		}
	}

	private URL uriInfo;
	private HttpHost host;
	private boolean followRedirects = RequestParams.enableFollowRedirects;
	private ProtocolVersion protocolVersion = RequestParams.protocolVersion;
	private Map<String, String> headerElements = new HashMap<>(8);
	private final StringBuilder requestStr = new StringBuilder(16);
	private List<String> cookies = new ArrayList<>(2);

	public HttpRequest(String url) throws MalformedURLException, UnsupportedEncodingException {
		this(url, MethodName.GET);
	}

	public HttpRequest(String url, MethodName methodName) throws MalformedURLException {
		uriInfo = new URL(url);
		host = new HttpHost(uriInfo.getHost(), uriInfo.getPort());
		writeRequestLine(methodName);
	}

	public void addEntry(String key, String value) {
		headerElements.put(key, value);
	}

	private void writeRequestLine(MethodName methodName) {
		String reuqestMethod = methodName.toString();
		requestStr.append(reuqestMethod).append(" ");//GET 
		requestStr.append(uriInfo.getPath());
		if (uriInfo.getQuery() != null) {
			requestStr.append("?");
			requestStr.append(uriInfo.getQuery());
		}
		requestStr.append(" ").append(protocolVersion.version).append(Header.CRLF);
		requestStr.append(HOST).append(COLON).append(uriInfo.getHost());
		if (uriInfo.getPort() != -1) {
			requestStr.append(COLON);
			requestStr.append(uriInfo.getPort());
		}
		requestStr.append(Header.CRLF);
	}

	/**
	 * 启用压缩,每次构建request后必须手动设置
	 */
	public void enableGzipCompress() {
		//headerElements.put(ACCEPT_ENCODING, DecoderUtils.acceptEncodingStr(inOrderAcceptEncodingList));
		headerElements.put(ACCEPT_ENCODING, "gzip");
	}

	public Map<String, String> getHeaderElements() {
		return headerElements;
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

	public void setCookie(String cookieValue) {
		if(cookieValue!=null && cookieValue.trim().length()>0){
			cookies.add(cookieValue);
		}
	}
	
	public byte[] getRequertData() throws UnsupportedEncodingException{
		for(Entry<String,String> entry: headerElements.entrySet()){
			requestStr.append(entry.getKey()).append(HttpRequest.COLON).append(entry.getValue()).append(Header.CRLF);
		}
		for(String cookie : cookies){
			requestStr.append("Cookie").append(HttpRequest.COLON).append(cookie).append(Header.CRLF);
		}
		requestStr.append(Header.CRLF);
		return requestStr.toString().getBytes("US-ASCII");
	}
}
