package com.tmall.search.httpclient.util;

/**
 * 存储chunkcontext信息
 * @author xiaolin.mxl
 */
public class ChunkContext {

	private byte[] chunkData = null;
	private int readLength;
	private int remaining;
	public byte[] getChunkData() {
		return chunkData;
	}
	public void setChunkData(byte[] chunkData) {
		this.chunkData = chunkData;
	}
	public int getReadLength() {
		return readLength;
	}
	public void setReadLength(int readLength) {
		this.readLength = readLength;
	}
	public int getRemaining() {
		return remaining;
	}
	public void setRemaining(int remaining) {
		this.remaining = remaining;
	}
}
