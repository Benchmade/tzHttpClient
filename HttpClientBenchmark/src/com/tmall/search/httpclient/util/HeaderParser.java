package com.tmall.search.httpclient.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.ProtocolException;

public final class HeaderParser {

	
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

	
	/**
	 * 处理List的headerline,转化为Header对象.
	 * @param statusList 
	 * @param fillElements	是否填充除了statuscode,ProtocolVersion和
	 * @return	返回Hader信息
	 * @throws ProtocolException
	 */
	public static Map<String,String> parser(List<String> statusList) throws ProtocolException {
		Map<String,String> elements = new HashMap<String, String>();
		String name, value;
		for (String headerLine : statusList) {
				int colon = headerLine.indexOf(':');
				if (colon < 0) {
					throw new ProtocolException("Unable to parse header: " + headerLine);
				}
				name = headerLine.substring(0, colon).trim();
				value = headerLine.substring(colon + 1).trim();
				/*if(name.equals(CONTENT_TYPE)){
					int csPos = value.indexOf('=');
					if(csPos!=-1){
						header.setCharset(value.substring(csPos+1));
					}
				}*/
				elements.put(name, value);
			}
		return elements;
	}
	
}
