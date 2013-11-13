package com.tmall.search.httpclient.params;

import com.tmall.search.httpclient.client.HttpRequest.ProtocolVersion;
import com.tmall.search.httpclient.compress.AcceptDecoder;
import com.tmall.search.httpclient.compress.GzipDecoder;

/**
 * Global Params
 * @author xiaolin.mxl
 */
public final class RequestParams {

	public static boolean enableFollowRedirects = true;//30X redirect
	public static ProtocolVersion protocolVersion = ProtocolVersion.HTTP11;
	public static AcceptDecoder defaultDecoder = new GzipDecoder();
	
}
