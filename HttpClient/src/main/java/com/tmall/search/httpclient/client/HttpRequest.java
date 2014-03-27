package com.tmall.search.httpclient.client;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
	private byte[] reqBody;
	private HttpHost host;
	private boolean followRedirects = RequestParams.enableFollowRedirects;
	private ProtocolVersion protocolVersion = RequestParams.protocolVersion;
	private Map<String, String> headerElements = new HashMap<>(8);
	private String requestLine;

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
		StringBuilder sb = new StringBuilder(16);
		String reuqestMethod = methodName.toString();
		sb.append(reuqestMethod).append(" ");//GET 
		sb.append(uriInfo.getPath());
		if (uriInfo.getQuery() != null) {
			sb.append("?");
			sb.append(uriInfo.getQuery());
		}
		sb.append(" ").append(protocolVersion.version).append(Header.CRLF);
		sb.append(HOST).append(COLON).append(uriInfo.getHost());
		if (uriInfo.getPort() != -1) {
			sb.append(COLON);
			sb.append(uriInfo.getPort());
		}
		sb.append(Header.CRLF);
		requestLine = sb.toString();
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

	public String getRequestLine() {
		return requestLine;
	}

	public byte[] getReqBody() {
		return reqBody;
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
			headerElements.put("Cookie", cookieValue);
		}
	}
}
