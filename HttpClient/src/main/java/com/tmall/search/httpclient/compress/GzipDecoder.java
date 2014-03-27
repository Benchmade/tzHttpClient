package com.tmall.search.httpclient.compress;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

public class GzipDecoder implements AcceptDecoder {

	@Override
	public byte[] uncompress(byte[] data) throws IOException {
		byte[] result = null;
		try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			GZIPInputStream gZIPInputStream = new GZIPInputStream(byteArrayInputStream);) {
			result = IOUtils.toByteArray(gZIPInputStream);
			byteArrayInputStream.close();
		}
		return result;
	}

	@Override
	public String getAlgorithmName() {
		return "gzip";
	}

}
