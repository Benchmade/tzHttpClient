package com.tmall.search.httpclient.client;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.tmall.search.httpclient.conn.HttpHost;
import com.tmall.search.httpclient.params.HttpMethodParams;
import com.tmall.search.httpclient.util.ProtocolException;

/**
 * httprequest请求数据描述类,没有写param类定义可配置参数.
 * @author xiaolin.mxl
 */
public class HttpRequest {

	public static final String ACCEPT_ENCODING = "Accept-Encoding";
	public static final String HOST = "Host";
	public static final String COLON = ":";
	public static final String DOLLAR="$";
	private static final HttpMethodParams defaultHttpMethodParams = new HttpMethodParams();
	//private static final Logger LOG = LogManager.getLogger(HttpRequest.class);
	/**
	 * @author xiaolin.mxl
	 */
	public static enum MethodName {
		GET, POST, HEAD;
		@Override
		public String toString() {
			return this.name().toUpperCase();
		}
	}

	private URL uriInfo;
	private HttpHost hostInfo;
	private String path; //解析cookie使用
	private String query; //解析cookie使用
	private Map<String, String> headerElements = new HashMap<>(8);
	private Set<String> cookies = new HashSet<>();
	private MethodName methodName;
	private HttpMethodParams httpMethodParams;
	private StringBuilder requestStr = new StringBuilder(16);

	public HttpRequest(String url) throws MalformedURLException, ProtocolException {
		this(url, MethodName.GET,defaultHttpMethodParams);
	}

	public HttpRequest(String url,HttpMethodParams httpMethodParams) throws MalformedURLException,ProtocolException {
		this(url, MethodName.GET,defaultHttpMethodParams);
	}
	
	public HttpRequest(String url, MethodName methodName, HttpMethodParams httpMethodParams) throws MalformedURLException ,ProtocolException{
		uriInfo = new URL(url);
		/*if("https".equalsIgnoreCase(uriInfo.getProtocol())){
			throw new ProtocolException("Does not support the https protocol!");
		}*/
		if("https".equalsIgnoreCase(uriInfo.getProtocol())){
			hostInfo = new HttpHost(uriInfo.getHost(), 443);
		}else{
			hostInfo = new HttpHost(uriInfo.getHost(), uriInfo.getPort());
		}
		hostInfo.setProtocol(uriInfo.getProtocol());
		this.path = uriInfo.getPath();
		this.query = uriInfo.getQuery();
		this.methodName = methodName;
		this.httpMethodParams = httpMethodParams;
		buildRequestLine();
	}
	
	public URL getUriInfo() {
		return uriInfo;
	}

	public void addEntry(String key, String value) {
		headerElements.put(key, value);
	}

	public Map<String, String> getHeaderElements() {
		return headerElements;
	}

	public HttpHost getHostInfo() {
		return hostInfo;
	}

	public MethodName getMethodName() {
		return methodName;
	}

	public void setMethodName(MethodName methodName) {
		this.methodName = methodName;
	}

	public void setCookies(Set<ClientCookie> cookieSet){
		if(cookieSet!=null){
			for(ClientCookie cookie : cookieSet){
				StringBuilder sb = new StringBuilder();
				sb.append(DOLLAR).append("Version=").append(cookie.getVersion()).append("; ");
				sb.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
				sb.append(DOLLAR).append("Path=").append(cookie.getPath()).append("; ");
				sb.append(DOLLAR).append("Domain=").append(cookie.getDomain());
				cookies.add(sb.toString());
			}
		}
	}
	
	public Set<String> getCookies() {
		return cookies;
	}

	public String getPath() {
		return path;
	}

	public String getQuery() {
		return query;
	}
	

	private void buildRequestLine(){
		String reuqestMethod = methodName.toString();
		requestStr.append(reuqestMethod).append(" ");//GET 
		requestStr.append(this.path);
		if (this.query != null) {
			requestStr.append("?");
			requestStr.append(this.query);
		}
		requestStr.append(" ").append(httpMethodParams.getProtocolVersion().getVersion()).append(Header.CRLF);
		requestStr.append(HttpRequest.HOST).append(HttpRequest.COLON).append(this.hostInfo.getHost());
		if(this.hostInfo.getPort()!=80){
			requestStr.append(HttpRequest.COLON);
			requestStr.append(this.hostInfo.getPort());
		}
		requestStr.append(Header.CRLF);
		//requestStr.append("User-Agent:Jakarta Commons-HttpClient/3.1");
		//requestStr.append(Header.CRLF);
	}
	
	public HttpMethodParams getHttpMethodParams() {
		return httpMethodParams;
	}

	public byte[] getOutputDate() throws UnsupportedEncodingException{
		if(httpMethodParams.isGzipCompress()){
			headerElements.put(ACCEPT_ENCODING, "gzip");
		}
		for(Entry<String,String> entry: headerElements.entrySet()){
			requestStr.append(entry.getKey()).append(HttpRequest.COLON).append(entry.getValue()).append(Header.CRLF);
		}
		for(String cookie : cookies){
			requestStr.append("Cookie").append(HttpRequest.COLON).append(cookie).append(Header.CRLF);
		}
		requestStr.append(Header.CRLF);
		//LOG.debug(requestStr);
		return requestStr.toString().getBytes();
	}
	
}
