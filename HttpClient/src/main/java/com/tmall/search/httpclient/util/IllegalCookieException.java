package com.tmall.search.httpclient.util;

/**
 * @author xiaolin.mxl
 */
public class IllegalCookieException extends HttpException{

	private static final long serialVersionUID = -274004164248182406L;

	/**
     * Creates a new IllegalChunkDataException with a <tt>null</tt> detail message. 
     */
    public IllegalCookieException() {
        super();
    }

    public IllegalCookieException(String message) {
        super(message);
    }

    public IllegalCookieException(String message, Throwable cause) {
        super(message, cause);
    }
	
}
