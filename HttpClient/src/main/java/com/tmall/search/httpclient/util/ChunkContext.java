package com.tmall.search.httpclient.util;

/**
 * 存储chunkcontext信息
 * @author xiaolin.mxl
 */
public class ChunkContext {

	private byte[] data = null;
	private int readLength;
	private int remaining;
	private int position;

	public void grow(int length) {
		if (data == null) {
			data = new byte[length];
		} else {
			byte[] newData = new byte[data.length + length];
			System.arraycopy(data, 0, newData, 0, data.length);
			data = newData;
		}
	}

	public void put(byte[] bufferData, int offset, int length) {
		System.arraycopy(bufferData, offset, data, position, length);
		position += length;
	}
	
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
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
