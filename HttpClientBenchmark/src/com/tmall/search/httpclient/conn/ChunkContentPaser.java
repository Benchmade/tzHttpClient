package com.tmall.search.httpclient.conn;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.tmall.search.httpclient.client.Header;
import com.tmall.search.httpclient.util.ByteUtil;
import com.tmall.search.httpclient.util.ChunkStateInfo;
import com.tmall.search.httpclient.util.HttpException;

public class ChunkContentPaser implements ContentPaser {

	@Override
	public byte[] paser(HttpConnection conn, Header header, ByteBuffer readBuffer) throws HttpException{
		byte[] respData;
		ByteBuffer buffer = readBuffer;
		ChunkStateInfo xiaolin = new ChunkStateInfo();
		boolean end = false;
		while (!end) {
			end = ByteUtil.isChunkEnd(xiaolin, buffer.array(), buffer.limit());

			ByteUtil.fillChunkBody(xiaolin, buffer.array(), xiaolin.getChunkData() == null ? header.getLength() : 0, buffer.limit());
			buffer.clear();
			if (!end) {
				try {
					buffer = conn.read();
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					throw new HttpException(e.getMessage());
				}
			}
		}
		respData = xiaolin.getChunkData();
		return respData;
	}
}
