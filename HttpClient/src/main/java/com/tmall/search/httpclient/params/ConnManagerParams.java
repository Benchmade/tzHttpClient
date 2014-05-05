package com.tmall.search.httpclient.params;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public final class ConnManagerParams {
	
	//设置conn的实现类
	private ConnImpl connImpl = ConnImpl.AIO;
	private static final Logger LOG = LogManager.getLogger(ConnManagerParams.class);
	int maxGlobalConnNum = 200;
	int perHostConnNum = 10;
	
	/**
	 * 链接数控制提到构造方法中设置，避免后期设置导致线程管理混乱.
	 * @param perHostConnNum
	 * @param maxGlobalConnNum
	 */
	public ConnManagerParams(int perHostConnNum,int maxGlobalConnNum) {
		if(maxGlobalConnNum>10){
			this.maxGlobalConnNum = maxGlobalConnNum;
		}else{
			LOG.warn("");
		}
		if(perHostConnNum>2){
			this.perHostConnNum = perHostConnNum;
		}else{
			LOG.warn("");
		}
	}
	
	public ConnManagerParams() {
	}
	
	public int getMaxGlobalConnNum() {
		return maxGlobalConnNum;
	}

	public int getPerHostConnNum() {
		return perHostConnNum;
	}

	public int getValue(Options key){
		return key.value;
	}

	public void set(Options key, int value){
		if(key==null || value<=0){
			return;
		}
		if(key==Options.SO_RCVBUF){//设置读取buffer时,最小为1024 * 4,保证一次读完header信息.
			if(value<1024 * 4){
				value = 1024 * 4;
			}
		}
		key.setValue(value);
	}
	
	
	public ConnImpl getConnImpl() {
		return connImpl;
	}

	public void setConnImpl(ConnImpl connImpl) {
		this.connImpl = connImpl;
	}





	public static enum Options{
		SO_RCVBUF(1024 * 8),//receive缓存size
		SO_SNDBUF(1024 * 4),	//send缓冲size
		CONNECTION_TIMEOUT(300),//连接服务器超时时间
		WRITE_TIMEOUT(200),//向服务器发送写请求超时
		READER_TIMROUT(1000),//读取response超时时间
		CONNECT_TIMEOUT_EXPIRE(10000),//keepAlive超时时间
		GET_CONN_WAIT_TIMEOUT(1000);//获得conn对象等待超时时间.高并发时为了防止conn过多内存溢出
		
		private int value;
		private Options(int value) {
			this.value = value;
		}
		public void setValue(int value){
			this.value = value;
		}
	}
	
	public static enum ConnImpl{
		AIO,NIO
	}
}
