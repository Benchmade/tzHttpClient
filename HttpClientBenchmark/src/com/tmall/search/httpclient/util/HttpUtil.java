package com.tmall.search.httpclient.util;


public final class HttpUtil {
	
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
}
