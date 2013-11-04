package com.tmall.search.httpclient.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.ProtocolException;

import com.tmall.search.httpclient.util.HeaderParser;
import com.tmall.search.httpclient.util.HttpUtil;
/**
 * httpheader描述类.未处理302跳转
 * @author xiaolin.mxl
 *
 */
public class Header {
	private Map<String, String> headerElements;
	private int statusCode;
	private String protocolVersion;
	private boolean isChunk = false;//是否chunk模式读取
	private int length;
	private boolean isClosed = false;//是否被服务器端关闭
	private boolean isCompressed = false;
	
	public Header(ByteBuffer buffer) throws ProtocolException {
		List<String> statusList = new ArrayList<String>();
		length = readHeader(buffer, statusList);
		if (length == -1) {
			throw new ProtocolException("Header Info is null.");
		}
		headerElements = HeaderParser.parser(statusList);
		if ("chunked".equalsIgnoreCase(headerElements.get("Transfer-Encoding"))) {
			isChunk = true;
		}
		if ("close".equals(headerElements.get("Connection"))) {//hr.getHeaderElements().get("Connection")!=null && hr.getHeaderElements().get("Connection").equals()
			isClosed = true;
		}
		if ("gzip".equals(headerElements.get("Content-Encoding"))) {//hr.getHeaderElements().get("Connection")!=null && hr.getHeaderElements().get("Connection").equals()
			isCompressed = true;
		}
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
					paserRequestLine(line,data);
				} else {
					statusList.add(line);
				}
				pos = i + 2;
				i++;
			}
		}
		return pos;
	}
	
	
	private int readHeader(ByteBuffer data, List<String> statusList) throws ProtocolException {
		int pos = 0;//记录每次header的信息起始位置.
		int maxPos = data.limit() - HttpUtil.CRLF.length();
		String line;
		for (int i = 0; i <= maxPos; i++) {
			if (data.get(i) == HttpUtil.CR && data.get(i + 1) == HttpUtil.LF) {
				if (pos == i) {
					pos = i + 2;
					break;
				}
				line = new String(data.array(), pos, i - pos);
				if (pos == 0) {//RequestLine
					paserRequestLine(line,data.array());
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
	private void paserRequestLine(String line,byte[] data) throws ProtocolException {
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

	public Map<String, String> getHeaderElements() {
		return headerElements;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getProtocolVersion() {
		return protocolVersion;
	}

	public boolean isChunk() {
		return isChunk;
	}

	public int getLength() {
		return length;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public boolean isCompressed() {
		return isCompressed;
	}
	
}
