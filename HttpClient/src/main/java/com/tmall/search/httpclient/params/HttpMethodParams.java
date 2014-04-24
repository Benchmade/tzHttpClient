package com.tmall.search.httpclient.params;


public class HttpMethodParams {
	
	private boolean followRedirect = true;//30X redirect
	private ProtocolVersion protocolVersion = ProtocolVersion.HTTP11;
	private boolean gzipCompress = false;
	
	public boolean isFollowRedirect() {
		return followRedirect;
	}

	public void setFollowRedirect(boolean followRedirect) {
		this.followRedirect = followRedirect;
	}

	public ProtocolVersion getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(ProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public boolean isGzipCompress() {
		return gzipCompress;
	}

	public void setGzipCompress(boolean gzipCompress) {
		this.gzipCompress = gzipCompress;
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
}
