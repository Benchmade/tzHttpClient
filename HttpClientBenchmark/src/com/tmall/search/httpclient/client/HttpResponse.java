package com.tmall.search.httpclient.client;

import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.ProtocolException;

import com.tmall.search.httpclient.util.ByteUtil;
import com.tmall.search.httpclient.util.HttpUtil;

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
		/*for (Entry<String, String> entry : headerElements.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
		}
		sb.append("\n");*/
		sb.append(new String(bodyData));
		return sb.toString();
	}

	/**
	 * 填充header信息的list,同时返回header信息结束的位置.
	 * @param data 请求返回数据.
	 * @param statusList	待fill的集合.
	 * @return	返回header结束的位置.
	 */
	private int readHeader(byte[] data, List<String> statusList) throws ProtocolException {
		int pos = 0;//记录每次header的信息起始位置.
		int maxPos = data.length - HttpUtil.CRLF.length();
		String line;
		for (int i = 0; i <= maxPos; i++) {
			if (data[i] == HttpUtil.CR && data[i + 1] == HttpUtil.LF) {
				if (pos == i) {
					pos = i + 2;
					break;
				}
				line = new String(data, pos, i - pos);
				if (pos == 0) {
					paserStatusCode(line);
				} else {
					statusList.add(line);
				}
				pos = i + 2;
				i++;
			}
		}
		return pos;
	}

	/**
	 * 处理状态行信息 HTTP/1.1 404 Not Found
	 * @param line
	 * @throws ProtocolException
	 */
	private void paserStatusCode(String line) throws ProtocolException {
		int versionPos = line.indexOf('/');
		if (versionPos == -1) {
			throw new ProtocolException("Invalid HTTP Protocol name: " + line);
		}
		int codePos = line.indexOf(' ', versionPos + 1);
		if (versionPos == -1) {
			throw new ProtocolException("Unable to parse HTTP-Version from the status line: " + line);
		}
		this.protocolVersion = line.substring(versionPos + 1, codePos);
		int sc = line.indexOf(' ', codePos + 1);
		if (sc == -1) {
			throw new ProtocolException("Unable to parse status code from the status line: " + line);
		}
		try {
			this.statusCode = Integer.parseInt(line.substring(codePos + 1, sc));
		} catch (NumberFormatException e) {
			throw new ProtocolException("Unable to parse status code from status line: '" + line + "'");
		}
	}

	public static void main(String[] args) {
	}

	public boolean isClose() {
		return isClose;
	}
	
}
