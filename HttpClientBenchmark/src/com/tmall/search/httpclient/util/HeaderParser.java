package com.tmall.search.httpclient.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.ProtocolException;

public final class HeaderParser {

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
