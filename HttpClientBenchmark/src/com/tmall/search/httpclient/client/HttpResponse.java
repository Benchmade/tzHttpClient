package com.tmall.search.httpclient.client;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import com.tmall.search.httpclient.util.ByteUtil;

public class HttpResponse {

	private byte[] bodyData;
	private Map<String, String> headerElements;
	private int statusCode;
	private String protocolVersion;
	private boolean isClosed;
	public HttpResponse(Header header, byte[] data) {
		bodyData = data;
		headerElements = header.getHeaderElements();
		statusCode = header.getStatusCode();
		protocolVersion = header.getProtocolVersion();
		isClosed = header.isClosed();
	}

	public byte[] getBodyData() {
		return bodyData;
	}

	/**
	 * 追加chunk的数据.
	 * @param chunkData
	 */
	public void appendData(byte[] chunkData) {
		bodyData = ByteUtil.mergeByteArray(bodyData, chunkData, chunkData.length);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getProtocolVersion() {
		return protocolVersion;
	}

	public Map<String, String> getHeaderElements() {
		return headerElements;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(statusCode).append("\n");
		for (Entry<String, String> entry : headerElements.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}
		sb.append("\n");
		sb.append(new String(bodyData));
		return sb.toString();
	}

	public static void main(String[] args) {
	}

	public boolean isClosed() {
		return isClosed;
	}
	
}
