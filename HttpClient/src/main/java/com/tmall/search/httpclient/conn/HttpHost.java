package com.tmall.search.httpclient.conn;

/**
 * 链接taget描述.
 * @author xiaolin.mxl
 *
 */
public final class HttpHost {

	private String host; //地址
	private int port;	//端口
	private String protocol = "http"; //协议
	
	public HttpHost(String host, int port) {
		this.host = host;
		if(port==-1){
			this.port = 80;
		}else{
			this.port = port;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null){
			return false;
		}
		boolean equal;
		HttpHost other = (HttpHost)obj;
		if(this.host.equals(other.host) && this.port==other.port && this.protocol.equals(other.protocol)){
			equal = true;
		}else{
			equal = false;
		}
		return equal;
	}
	
	@Override
	public String toString() {
		return host + ":" +port;
	}
	
	@Override
	public int hashCode() {
		return host.hashCode()+ port + protocol.hashCode();
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
}
