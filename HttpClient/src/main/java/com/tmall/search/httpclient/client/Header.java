package com.tmall.search.httpclient.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tmall.search.httpclient.util.ProtocolException;
/**
 * httpheader描述类.未处理302跳转
 * @author xiaolin.mxl
 *
 */
public class Header {
	
	public static final int CR = 13; // <US-ASCII CR, carriage return (13)>
	public static final int LF = 10; // <US-ASCII LF, linefeed (10)>
	public static final String CRLF = "\r\n";
	public static final int SP = 32; // <US-ASCII SP, space (32)>
	public static final int HT = 9; // <US-ASCII HT, horizontal-tab (9)>

	/** HTTP header definitions */
	public static final String TRANSFER_ENCODING = "Transfer-Encoding";
	public static final String CONTENT_LEN = "Content-Length";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CONTENT_ENCODING = "Content-Encoding";
	public static final String EXPECT_DIRECTIVE = "Expect";
	public static final String CONN_DIRECTIVE = "Connection";
	public static final String TARGET_HOST = "Host";
	public static final String USER_AGENT = "User-Agent";
	public static final String DATE_HEADER = "Date";
	public static final String SERVER_HEADER = "Server";

	/** HTTP expectations */
	public static final String EXPECT_CONTINUE = "100-continue";

	/** HTTP connection control */
	public static final String CONN_CLOSE = "Close";
	public static final String CONN_KEEP_ALIVE = "Keep-Alive";

	/** Transfer encoding definitions */
	public static final String CHUNK_CODING = "chunked";
	public static final String IDENTITY_CODING = "identity";
	
	
	
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
		headerElements = headerConverter(statusList);
		if (CHUNK_CODING.equalsIgnoreCase(headerElements.get(TRANSFER_ENCODING))) {
			isChunk = true;
		}
		if (CONN_CLOSE.equalsIgnoreCase(headerElements.get(CONN_DIRECTIVE))) {
			isClosed = true;
		}
		if ("gzip".equals(headerElements.get(CONTENT_ENCODING))) {
			isCompressed = true;
		}
	}
	
	private Map<String,String> headerConverter(List<String> statusList) throws ProtocolException {
		Map<String,String> elements = new HashMap<String, String>();
		String name, value;
		for (String headerLine : statusList) {
				int colon = headerLine.indexOf(':');
				if (colon < 0) {
					throw new ProtocolException("Unable to parse header: " + headerLine);
				}
				name = headerLine.substring(0, colon).trim();
				value = headerLine.substring(colon + 1).trim();
				elements.put(name, value);
			}
		return elements;
	}
	
	private int readHeader(ByteBuffer data, List<String> statusList) throws ProtocolException {
		int pos = 0;//记录每次header的信息起始位置.
		int maxPos = data.limit() - Header.CRLF.length();
		String line;
		for (int i = 0; i <= maxPos; i++) {
			if (data.get(i) == Header.CR && data.get(i + 1) == Header.LF) {
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
