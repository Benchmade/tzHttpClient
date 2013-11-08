package com.tmall.search.httpclient.params;

import java.util.ArrayList;
import java.util.List;

import com.tmall.search.httpclient.client.HttpRequest.ProtocolVersion;
import com.tmall.search.httpclient.compress.AcceptDecoder;
import com.tmall.search.httpclient.compress.GzipDecoder;

/**
 * Global Params
 * @author xiaolin.mxl
 */
public final class RequestParams {

	public static boolean enableCompass = true;
	public static boolean enableFollowRedirects = true;//30X redirect
	public static ProtocolVersion protocolVersion = ProtocolVersion.HTTP11;
	public static List<AcceptDecoder> inOrderAcceptEncodingList = new ArrayList<AcceptDecoder>();
	static{
		inOrderAcceptEncodingList = new ArrayList<AcceptDecoder>();
		inOrderAcceptEncodingList.add(new GzipDecoder());
	}
	
}
