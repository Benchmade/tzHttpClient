package com.tmall.search.httpclient.util;

public class IllegalChunkDataException extends ProtocolException{

	private static final long serialVersionUID = -274004164248182406L;

	/**
     * Creates a new IllegalChunkDataException with a <tt>null</tt> detail message. 
     */
    public IllegalChunkDataException() {
        super();
    }

    public IllegalChunkDataException(String message) {
        super(message);
    }

    public IllegalChunkDataException(String message, Throwable cause) {
        super(message, cause);
    }
	
}
