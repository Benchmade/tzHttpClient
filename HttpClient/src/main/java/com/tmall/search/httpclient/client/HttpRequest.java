package com.tmall.search.httpclient.client;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.tmall.search.httpclient.conn.HttpHost;
import com.tmall.search.httpclient.params.RequestParams;
import com.tmall.search.httpclient.util.ByteUtil;

/**
 * httprequest请求数据描述类,没有写param类定义可配置参数.
 * @author xiaolin.mxl
 *
 */
public class HttpRequest {

	public static final String ACCEPT_ENCODING = "Accept-Encoding";
	public static final String HOST = "Host";
	public static final String COLON = ": ";

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

	private URI uriInfo;
	private byte[] reqBody;
	private HttpHost host;
	private String cookieValue = null;
	private boolean followRedirects = RequestParams.enableFollowRedirects;
	private ProtocolVersion protocolVersion = RequestParams.protocolVersion;
	private Map<String, String> headerElements = new HashMap<String, String>(8);
	private String requestLine;

	public HttpRequest(String url) throws URISyntaxException, UnsupportedEncodingException {
		this(url, MethodName.GET);
	}

	public HttpRequest(String url, MethodName methodName) throws URISyntaxException {
		uriInfo = new URI(url);
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
		if (uriInfo.getRawQuery() != null) {
			sb.append("?");
			sb.append(uriInfo.getRawQuery());
		}
		sb.append(" ").append(protocolVersion.version).append(Header.CRLF);
		sb.append(HOST).append(COLON).append(uriInfo.getHost());
		if (uriInfo.getPort() != -1) {
			sb.append(":");
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

	public String getCookieValue() {
		return cookieValue;
	}

	public void setCookieValue(String cookieValue) {
		this.cookieValue = cookieValue;
	}

	public ProtocolVersion getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(ProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public static void main(String[] args) throws Exception {
		HttpRequest m = new HttpRequest("http://www.amazon.cn/b/ref=sa_menu_softwa_l3_b811142051?ie=UTF8&node=811142051");
		System.out.println(new String(ByteUtil.assemblyRequestBody(m.getRequestLine(), m.getHeaderElements())));
	}

}
