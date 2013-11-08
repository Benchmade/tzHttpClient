package com.tmall.search.httpclient.compress;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

public class GzipDecoder implements AcceptDecoder {

	@Override
	public byte[] uncompress(byte[] data) throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
		GZIPInputStream gZIPInputStream = null;
		byte[] result = null;
		try {
			gZIPInputStream = new GZIPInputStream(byteArrayInputStream);
			result = IOUtils.toByteArray(gZIPInputStream);
			byteArrayInputStream.close();
		} finally{
			try {
				byteArrayInputStream.close();
				if(gZIPInputStream!=null){
					gZIPInputStream.close();
				}
			} catch (IOException e) {
				LOG.error("", e);
			}
		}
		return result;
	}

	@Override
	public String getAlgorithmName() {
		return "gzip";
	}

}
