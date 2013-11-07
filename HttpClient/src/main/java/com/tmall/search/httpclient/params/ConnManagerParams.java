package com.tmall.search.httpclient.params;

public final class ConnManagerParams {//待修改

	public static enum Options{
		SO_RCVBUF(1024 * 8),//receive缓存size
		SO_SNDBUF(1024 * 2),	//send缓冲size
		CONNECTION_TIMEOUT(500),//连接服务器超时时间
		WRITE_TIMEOUT(200),//向服务器发送写请求超时
		READER_TIMROUT(8000),//读取response超时时间
		CONNECT_TIMEOUT_EXPIRE(10000),//keepAlive超时时间
		GET_CONN_WAIT_TIMEOUT(2000),//获得conn对象等待超时时间.高并发时为了防止conn过多内存溢出
		CONN_MAX_NUM_PER_HOST(5),//每个host最大拥有的连接数.
		MAX_TOTAL_HOST(200);
		private int value;
		private Options(int value) {
			this.value = value;
		}
		public void setValue(int value){
			this.value = value;
		}
	}
	
	public int getValue(Options key){
		return key.value;
	}

	public void set(Options key, int value){
		if(key==Options.SO_RCVBUF){//设置读取buffer时,最小为512,保证一次读完header信息.
			if(value<512){
				value = 512;
			}
		}
		key.setValue(value);
	}
	
	/*//conn最大请求次数
	private int connMaxTime = 100000;*/
	
}
