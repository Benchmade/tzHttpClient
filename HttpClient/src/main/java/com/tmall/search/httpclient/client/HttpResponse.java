package com.tmall.search.httpclient.client;

import java.util.List;
import java.util.Map.Entry;

import com.tmall.search.httpclient.util.ByteUtils;

public class HttpResponse {

	private byte[] bodyData;
	private int statusCode;
	private String protocolVersion;
	private boolean isClosed; //
	private Header header;
	
	public HttpResponse(Header header, byte[] data) {
		this.bodyData = data;
		this.header = header;
		this.statusCode = header.getStatusCode();
		this.protocolVersion = header.getProtocolVersion();
		this.isClosed = header.isClosed();
	}

	public byte[] getBodyData() {
		return bodyData;
	}

	/**
	 * 追加chunk的数据.
	 * @param chunkData
	 */
	public void appendData(byte[] chunkData) {
		bodyData = ByteUtils.mergeByteArray(bodyData, chunkData, chunkData.length);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getProtocolVersion() {
		return protocolVersion;
	}

	public Header getHeader() {
		return header;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(statusCode).append("\n");
		for (Entry<String, List<String>> entry : header.getHeaderElementsMap().entrySet()) {
			for(String val : entry.getValue()){
				sb.append(entry.getKey()).append(": ").append(val).append("\n");
			}
		}
		//sb.append("\n");
		//sb.append(new String(bodyData));
		return sb.toString();
	}

	public boolean isClosed() {
		return isClosed;
	}
	
}
