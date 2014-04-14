package com.tmall.search.httpclient.params;

import com.tmall.search.httpclient.compress.AcceptDecoder;
import com.tmall.search.httpclient.compress.GzipDecoder;

public class HttpMethodParams {
	
	private boolean followRedirect = true;//30X redirect
	private AcceptDecoder defaultDecoder = new GzipDecoder();
	private ProtocolVersion protocolVersion = ProtocolVersion.HTTP11;
	private boolean onlyResponesHeaders = false;
	
	public boolean isFollowRedirect() {
		return followRedirect;
	}

	public void setFollowRedirect(boolean followRedirect) {
		this.followRedirect = followRedirect;
	}


	public AcceptDecoder getDefaultDecoder() {
		return defaultDecoder;
	}
	
	
	public ProtocolVersion getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(ProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public boolean isOnlyResponesHeaders() {
		return onlyResponesHeaders;
	}

	public void setOnlyResponesHeaders(boolean onlyResponesHeaders) {
		this.onlyResponesHeaders = onlyResponesHeaders;
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
