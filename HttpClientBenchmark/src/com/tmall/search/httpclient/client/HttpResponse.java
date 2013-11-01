package com.tmall.search.httpclient.client;

import java.util.Map;
import java.util.Map.Entry;

import com.tmall.search.httpclient.util.ByteUtil;

public class HttpResponse {

	private byte[] bodyData;
	private Map<String, String> headerElements;
	private int statusCode;
	private String protocolVersion;
	private boolean isClose;
	public HttpResponse(Header header, byte[] data) {
		bodyData = data;
		headerElements = header.getHeaderElements();
		statusCode = header.getStatusCode();
		protocolVersion = header.getProtocolVersion();
		isClose = header.isClose();
	}

	/*public HttpResponse(byte[] data) throws ProtocolException {
		List<String> statusList = new ArrayList<String>();
		int offset = readHeader(data, statusList);
		if (offset == -1) {
			throw new ProtocolException("Header Info is null.");
		}
		headerElements = HeaderParser.parser(statusList);
		if ("chunked".equalsIgnoreCase(headerElements.get("Transfer-Encoding"))) {
			bodyData = ByteUtil.getChunkData(data, offset).getChunkData();
		} else {
			bodyData = new byte[data.length - offset];
			System.arraycopy(data, offset, bodyData, 0, data.length - offset);
		}
	}
*/
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
		for (Entry<String, String> entry : headerElements.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}
		sb.append("\n");
		sb.append(new String(bodyData));
		return sb.toString();
	}

	public static void main(String[] args) {
	}

	public boolean isClose() {
		return isClose;
	}
	
}
