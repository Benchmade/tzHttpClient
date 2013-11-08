package com.tmall.search.httpclient.compress;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public interface AcceptDecoder {
	public static final Logger LOG = LogManager.getLogger(AcceptDecoder.class);
	public byte[] uncompress(byte[] data) throws IOException;
	public String getAlgorithmName();
}
