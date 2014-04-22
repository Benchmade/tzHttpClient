package com.tmall.search.httpclient.util;

import java.io.IOException;

public class RequestException extends IOException {

	private static final long serialVersionUID = -4626805797865100882L;

	public RequestException() {
		super();
	}
	
	public RequestException(String message) {
		super(message);
	}
	
	public RequestException(String message, Throwable cause) {
		 super(message, cause);
	}
}
