package com.tmall.search.httpclient.conn;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.tmall.search.httpclient.client.HttpRequest;
import com.tmall.search.httpclient.params.ConnManagerParams;
import com.tmall.search.httpclient.params.ConnManagerParams.Options;
import com.tmall.search.httpclient.util.HttpException;

/**
 * Async impl
 * @author xiaolin.mxl
 */
public class ASyncConnectionImpl implements HttpConnection {
	public static final Logger LOG = LogManager.getLogger(ASyncConnectionImpl.class);
	private AsynchronousSocketChannel client;
	private final ByteBuffer readbuffer;
	private final ByteBuffer writebuffer;
	private ConnManagerParams connParams;

	private long lastUseTime = Long.MAX_VALUE;

	public ASyncConnectionImpl(HttpHost host, ConnManagerParams connParams) throws HttpException {
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

	@Override
	public ByteBuffer read() throws HttpException {
		readbuffer.clear();
		try {
			client.read(readbuffer).get(this.connParams.getValue(Options.READER_TIMROUT), TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new HttpException("Read Server request error", e);
		}
		readbuffer.flip();
		return readbuffer;
	}

	@Override
	public void sendRequest(byte[] requestData) throws HttpException {
		writebuffer.clear();
		try {
			//LOG.debug("request : " + new String(requestData));
			lastUseTime = System.currentTimeMillis();//设置过期检测时间
			if (requestData.length > writebuffer.capacity()) {
				int sy = requestData.length;
				int pos = 0;
				while (sy > 0) {
					sy = sy - writebuffer.capacity();
					writebuffer.put(requestData, pos, sy > 0 ? writebuffer.capacity() : writebuffer.capacity() + sy);
					writebuffer.flip();
					client.write(writebuffer).get(this.connParams.getValue(Options.WRITE_TIMEOUT), TimeUnit.MILLISECONDS);
					writebuffer.clear();
					pos += writebuffer.capacity();
				}
			} else {
				writebuffer.put(requestData);
				writebuffer.flip();
				client.write(writebuffer).get(this.connParams.getValue(Options.WRITE_TIMEOUT), TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new HttpException("Failure to send data.", e);
		}
		writebuffer.clear();
	}

	@Override
	public void close() throws IOException {
		client.close();
	}
	
	@Override
	public long getLastUseTime() {
		return lastUseTime;
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
		if (System.currentTimeMillis() - lastUseTime > deadline) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String getRemoteAddress() {
		String result = "";
		try {
			result = client.getRemoteAddress().toString();
		} catch (IOException e) {
			LOG.error("getRemoteAddress error.", e);
		}
		return result;
	}
}
