package com.tmall.search.httpclient.conn;

/**
 * 链接taget描述.
 * @author xiaolin.mxl
 *
 */
public final class HttpHost {

	private String host; //地址
	private int port;	//端口
	
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
		HttpHost other = (HttpHost)obj;
		boolean equal;
		if(this.host.equals(other.host) && this.port==other.port){
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
		return host.hashCode()+ port;
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
	
	
	
}
