package com.tmall.search.httpclient.conn;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.tmall.search.httpclient.client.Header;
import com.tmall.search.httpclient.client.HttpRequest;
import com.tmall.search.httpclient.client.HttpResponse;
import com.tmall.search.httpclient.params.ConnManagerParams;
import com.tmall.search.httpclient.params.ConnManagerParams.Options;
import com.tmall.search.httpclient.util.ByteUtil;
import com.tmall.search.httpclient.util.ChunkStateInfo;
import com.tmall.search.httpclient.util.HttpException;
import com.tmall.search.httpclient.util.ProtocolException;

public class AIOConnectionImpl implements HttpConnection {

	private AsynchronousSocketChannel client;
	private final ByteBuffer readbuffer;
	private final ByteBuffer writebuffer;
	private ConnManagerParams connParams;
	//当前conn链接执行的次数.
	private int executeCount = 0;
	private long connectTime = Long.MAX_VALUE;

	public AIOConnectionImpl(HttpHost host, ConnManagerParams connParams) throws HttpException {
		this.connParams = connParams;
		try {
			client = AsynchronousSocketChannel.open();
			if (client.isOpen()) {
				client.setOption(StandardSocketOptions.SO_RCVBUF, this.connParams.getValue(Options.SO_RCVBUF));
				client.setOption(StandardSocketOptions.SO_SNDBUF, this.connParams.getValue(Options.SO_SNDBUF));
				client.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
				client.connect(new InetSocketAddress(host.getHost(), host.getPort())).get(this.connParams.getValue(Options.CONNECTION_TIMEOUT),
						TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
			throw new HttpException("Create Connection error ", e);
		}
		readbuffer = ByteBuffer.allocate(this.connParams.getValue(Options.SO_RCVBUF));
		writebuffer = ByteBuffer.allocate(this.connParams.getValue(Options.SO_SNDBUF));

	}

	public HttpResponse readResp() throws InterruptedException, ExecutionException, TimeoutException, ProtocolException {
		int readLength = client.read(readbuffer).get(this.connParams.getValue(Options.READER_TIMROUT), TimeUnit.MILLISECONDS);
		readbuffer.flip();
		Header header = new Header(readbuffer);//第一次读取出header信息
		byte[] respData = null;
		if (header.isChunk()) {
			ChunkStateInfo xiaolin = new ChunkStateInfo();
			boolean end = false;
			while (!end) {
				end = ByteUtil.isChunkEnd(xiaolin, readbuffer.array(), readbuffer.position());
				/*if(readbuffer.position()>4){
					System.out.println(readbuffer.get(readbuffer.position()-4) + "," + readbuffer.get(readbuffer.position()-3) + "," +readbuffer.get(readbuffer.position()-2) + "," + readbuffer.get(readbuffer.position()-1));
					if(readbuffer.get(readbuffer.position()-2)==52 && readbuffer.get(readbuffer.position()-1)==48){
						System.out.println();
					}
				}*/
				ByteUtil.fillChunkBody(xiaolin, readbuffer.array(), xiaolin.getChunkData() == null ? header.getLength() : 0, readbuffer.position());
				readbuffer.clear();
				if (!end) {
					readLength = client.read(readbuffer).get(this.connParams.getValue(Options.READER_TIMROUT), TimeUnit.MILLISECONDS);
				}
			}
			respData = xiaolin.getChunkData();
		} else {
			respData = ByteUtil.mergeByteArray(respData, readbuffer.array(), header.getLength(), readLength - header.getLength());
			int sy = Integer.parseInt(header.getHeaderElements().get("Content-Length")) - (readLength - header.getLength());
			readbuffer.clear();
			while (sy > 0) {// 如果读取长度和容量一样,可能没有读取完,需要再次读取
				readLength = client.read(readbuffer).get(this.connParams.getValue(Options.READER_TIMROUT), TimeUnit.MILLISECONDS);
				sy = sy - readLength;
				respData = ByteUtil.mergeByteArray(respData, readbuffer.array(), readLength);
				readbuffer.clear();
			}
		}
		readbuffer.clear();
		HttpResponse hr = new HttpResponse(header, respData);
		return hr;
	}

	@Override
	public ByteBuffer read() throws InterruptedException, ExecutionException, TimeoutException {
		readbuffer.clear();
		client.read(readbuffer).get(this.connParams.getValue(Options.READER_TIMROUT), TimeUnit.MILLISECONDS);
		readbuffer.flip();
		return readbuffer;
	}

	@Override
	public void sendRequest(HttpRequest method) throws InterruptedException, ExecutionException, TimeoutException {
		writebuffer.clear();
		byte[] requestData = method.getSendData();
		connectTime = System.currentTimeMillis();//设置过期检测时间
		if (requestData.length > writebuffer.capacity()) {
			int sy = requestData.length;
			int pos = 0;
			while (sy > 0) {
				sy = sy - writebuffer.capacity();
				writebuffer.put(method.getSendData(), pos, sy > 0 ? writebuffer.capacity() : writebuffer.capacity() + sy);
				writebuffer.flip();
				client.write(writebuffer).get(this.connParams.getValue(Options.WRITE_TIMEOUT), TimeUnit.MILLISECONDS);
				writebuffer.clear();
				pos += writebuffer.capacity();
			}
		} else {
			writebuffer.put(method.getSendData());
			writebuffer.flip();
			client.write(writebuffer).get(this.connParams.getValue(Options.WRITE_TIMEOUT), TimeUnit.MILLISECONDS);
		}
		writebuffer.clear();
	}

	@Override
	public void close() throws IOException {
		client.shutdownInput();
		client.shutdownOutput();
		client.close();
		client = null;
	}

	/**
	 * 判断链接是否过期,如果链接在超时时间内,并且连接数在限制范围内,那么设置这个链接为过期链接.
	 * @return 如果过期true  没有过期返回false
	 */
	@Override
	public boolean isExpired() {
		return this.isExpired(this.connParams.getValue(Options.CONNECT_TIMEOUT_EXPIRE), TimeUnit.MILLISECONDS);
	}

	@Override
	public boolean isExpired(long idletime, TimeUnit tunit) {
		//if (this.connParams.getValue(Options.) > executeCount && System.currentTimeMillis() - connectTime < this.connParams.getConnTimeOutExpire()) {
		long deadline = tunit.toMillis(idletime);
		if (System.currentTimeMillis() - connectTime > deadline) {
			return true;
		} else {
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
		AIOConnectionImpl client = new AIOConnectionImpl(new HttpHost("10.232.43.8", 8000), new ConnManagerParams());
		for (int i = 0; i < 1; i++) {
			HttpRequest req = new HttpRequest("http://10.232.43.8:8000/qp?s=relasearchmall&c=2&src=tmall-search_10.72.87.152&k=nike");
			client.sendRequest(req);
			System.out.println(new String(client.readResp().getBodyData()));
		}
		client.close();
		byte[] s = new byte[] { 54, 51, 101 };
		System.out.println(Integer.parseInt(new String(s), 16));
	}

}
