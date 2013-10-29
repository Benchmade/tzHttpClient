package com.tmall.search.httpclient.conn;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.tmall.search.httpclient.client.HttpRequest;
import com.tmall.search.httpclient.params.ConnManagerParams;
import com.tmall.search.httpclient.params.ConnManagerParams.Options;
import com.tmall.search.httpclient.util.ByteUtil;

public class NIOConnectionImpl {//implements HttpConnection {
	private SocketChannel client;
	private final ByteBuffer readbuffer;
	private final ByteBuffer writebuffer;
	private ConnManagerParams connParams;
	private int max = 0;
	private long connectTime = Long.MAX_VALUE;
	
	public NIOConnectionImpl(HttpHost host,ConnManagerParams connParams) throws IOException, InterruptedException, ExecutionException, TimeoutException {
		client = SocketChannel.open();
		if (client.isOpen()) {
			client.setOption(StandardSocketOptions.SO_RCVBUF, this.connParams.getValue(Options.SO_RCVBUF));
			client.setOption(StandardSocketOptions.SO_SNDBUF, this.connParams.getValue(Options.SO_SNDBUF));
			client.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			client.setOption(StandardSocketOptions.SO_LINGER,0);
			Socket socket = client.socket();
			socket.setSoTimeout(1);
			socket.connect(new InetSocketAddress(host.getHost(), host.getPort()), this.connParams.getValue(Options.CONNECTION_TIMEOUT));
		}
		this.connParams = connParams;
		readbuffer = ByteBuffer.allocate(this.connParams.getValue(Options.SO_RCVBUF));
		writebuffer = ByteBuffer.allocate(this.connParams.getValue(Options.SO_SNDBUF));
	}

	public byte[] sendRequest(HttpRequest method) throws Exception {
		max++;
		writebuffer.put(method.getSendData());
		writebuffer.flip();
		client.write(writebuffer);
		writebuffer.clear();
		byte[] resultArray = null;
		long readLength;
		do {
			readLength = client.read(readbuffer);
			if (readLength > 0) {// 如果读取长度大于0,才进行
				resultArray = ByteUtil.mergeByteArray(resultArray, readbuffer.array(), (int) readLength);
			}
			readbuffer.clear();// 保证每次使用完都复位
		} while (readLength == readbuffer.capacity());// 如果读取长度和容量一样,可能没有读取完,需要再次读取
		return resultArray;
	}

	public void close() throws IOException {
		client.close();
	}

	public boolean isNotExpired() {
		if(System.currentTimeMillis()-connectTime < this.connParams.getValue(Options.CONNECT_TIMEOUT_EXPIRE)){
			return true;
		}else{
			return false;
		}
	}
	

	public ByteBuffer nextChunk() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args) throws Exception{

		long s = System.currentTimeMillis();
		NIOConnectionImpl a = new NIOConnectionImpl(new HttpHost("localhost", 8080),new ConnManagerParams());
		for (int i = 0; i < 100; i++) {
			System.out.println(new String(a.sendRequest(new HttpRequest("http://localhost:8080/BenchmadeWeb/xxx?q=" + i))));
			System.out.println(i);
		}
		a.close();
		System.out.println(System.currentTimeMillis() - s);
	}
}
