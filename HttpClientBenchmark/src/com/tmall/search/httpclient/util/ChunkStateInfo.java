package com.tmall.search.httpclient.util;

public class ChunkStateInfo {

	private byte[] chunkData = null;
	//当前buffer,Content-Length读取完,但没有读取到CRLF,或者读取完一个chunk,读取下个个chunk的length时,无法找到结尾.放入剩余数据,与下次读取的是buffer合并
	private byte[] lastBuffRemaining = null;
	//当前buffer,读取到一个chunk的size末尾,但是没有读取到结束符.下次读取的时候,直接跳过剩余的byte后,开始读取下一个chunk的size
	private int unFinishedNum = 0;
	private int shengyu = 0;
	public byte[] getChunkData() {
		return chunkData;
	}
	public void setChunkData(byte[] chunkData) {
		this.chunkData = chunkData;
	}
	
	public int getUnFinishedNum() {
		return unFinishedNum;
	}
	public void setUnFinishedNum(int unFinishedNum) {
		this.unFinishedNum = unFinishedNum;
	}
	public int getShengyu() {
		return shengyu;
	}
	public void setShengyu(int shengyu) {
		this.shengyu = shengyu;
	}
	public byte[] getLastBuffRemaining() {
		return lastBuffRemaining;
	}
	public void setLastBuffRemaining(byte[] lastBuffRemaining) {
		this.lastBuffRemaining = lastBuffRemaining;
	}
}
