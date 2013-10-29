package com.tmall.search.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.tmall.search.httpclient.client.HttpRequest;
import com.tmall.search.httpclient.util.ByteUtil;

public class NIOHttpClient {
	private SocketChannel client;
	private final ByteBuffer readbuffer = ByteBuffer.allocate(4 * 1024);
	private final ByteBuffer writebuffer = ByteBuffer.allocate(1024);

	public NIOHttpClient() throws IOException, InterruptedException, ExecutionException, TimeoutException {
		client = SocketChannel.open();
		if (client.isOpen()) {
			client.setOption(StandardSocketOptions.SO_RCVBUF, 2 * 1024);
			client.setOption(StandardSocketOptions.SO_SNDBUF, 1024);
			client.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			// client.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			//client.connect(new InetSocketAddress("localhost", 8080));
			client.connect(new InetSocketAddress("10.232.43.8", 8000));
		}
	}

	public byte[] execute(HttpRequest method) throws InterruptedException, ExecutionException, TimeoutException, IOException {
		writebuffer.put(method.getSendData());
		writebuffer.flip();
		int len = client.write(writebuffer);
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
		readLength = client.read(readbuffer);
		resultArray = ByteUtil.mergeByteArray(resultArray, readbuffer.array(), (int) readLength);
		readbuffer.clear();// 保证每次使用完都复位
		return resultArray;
	}

	public void close() throws IOException {
		client.close();
	}

	public static void main(String[] args) throws Exception {

		long s = System.currentTimeMillis();
		NIOHttpClient a = new NIOHttpClient();
		for (int i = 0; i < 1; i++) {
			System.out.println(new String(a.execute(new HttpRequest("http://localhost:8080/BenchmadeWeb/xxx?q=" + i))));
			System.out.println(new String(a.execute(new HttpRequest("http://localhost:8080/BenchmadeWeb/xxx?q=6789"))));
			//System.out.println(new String(a.execute(new HttpRequest("http://10.232.43.8:8000/qp?s=relasearchmall&c=2&src=tmall-search_10.72.87.152&k=nike"))));
		}
		a.close();
	}
}
