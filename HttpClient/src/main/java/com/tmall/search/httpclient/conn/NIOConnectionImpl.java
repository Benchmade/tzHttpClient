package com.tmall.search.httpclient.conn;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.tmall.search.httpclient.client.HttpRequest;
import com.tmall.search.httpclient.params.ConnManagerParams;
import com.tmall.search.httpclient.params.ConnManagerParams.Options;
import com.tmall.search.httpclient.util.ByteUtil;
import com.tmall.search.httpclient.util.HttpException;

public class NIOConnectionImpl implements HttpConnection {
	public static final Logger LOG = LogManager.getLogger(NIOConnectionImpl.class);
	private SocketChannel client; 
	private final ByteBuffer readbuffer;
	private final ByteBuffer writebuffer;
	private ConnManagerParams connParams;
	private long lastUseTime = Long.MAX_VALUE;
	private final static ExecutorService executorService = Executors.newCachedThreadPool();
	
	public NIOConnectionImpl(final HttpHost host, ConnManagerParams connParams) throws HttpException{
		this.connParams = connParams;
		try {
			client = SocketChannel.open();
			Future<Boolean> future= executorService.submit(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return client.connect(new InetSocketAddress(host.getHost(), host.getPort()));
				}
			});
			future.get(this.connParams.getValue(Options.CONNECT_TIMEOUT_EXPIRE),TimeUnit.MILLISECONDS);
		//} catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
		} catch (Exception e) {
			throw new HttpException("Can't create connection "+ host.toString(), e);
		}
		readbuffer = ByteBuffer.allocate(this.connParams.getValue(Options.SO_RCVBUF));
		writebuffer = ByteBuffer.allocate(this.connParams.getValue(Options.SO_SNDBUF));
	}
	
	@Override
	public void sendRequest(HttpRequest method) throws HttpException {
		
		writebuffer.clear();
		try {
			byte[] requestData = ByteUtil.assemblyRequestBody(method.getRequestLine(), method.getHeaderElements());
			//LOG.debug("request : " + new String(requestData));
			lastUseTime = System.currentTimeMillis();//设置过期检测时间
			if (requestData.length > writebuffer.capacity()) {
				int sy = requestData.length;
				int pos = 0;
				while (sy > 0) {
					sy = sy - writebuffer.capacity();
					writebuffer.put(requestData, pos, sy > 0 ? writebuffer.capacity() : writebuffer.capacity() + sy);
					writebuffer.flip();
					executorService.submit(new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							return client.write(writebuffer);
						}
					}).get(this.connParams.getValue(Options.WRITE_TIMEOUT), TimeUnit.MILLISECONDS);
					writebuffer.clear();
					pos += writebuffer.capacity();
				}
			} else {
				writebuffer.put(requestData);
				writebuffer.flip();
				executorService.submit(new Callable<Integer>() {
					@Override
					public Integer call() throws Exception {
						return client.write(writebuffer);
					}
				}).get(this.connParams.getValue(Options.WRITE_TIMEOUT), TimeUnit.MILLISECONDS);
			}
		//} catch (InterruptedException | ExecutionException | TimeoutException | UnsupportedEncodingException e) {
		} catch (Exception e) {
			throw new HttpException("Failure to send data.", e);
		}
		writebuffer.clear();
		
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

	@Override
	public ByteBuffer read() throws HttpException {
		readbuffer.clear();
		try {
			executorService.submit(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					return client.read(readbuffer);
				}
			})
			.get(this.connParams.getValue(Options.READER_TIMROUT), TimeUnit.MILLISECONDS);
		//} catch (InterruptedException | ExecutionException | TimeoutException e) {
		} catch (Exception e) {
			throw new HttpException("Read Server request error", e);
		}
		readbuffer.flip();
		return readbuffer;
	}

	@Override
	public boolean isExpired() {
		return this.isExpired(this.connParams.getValue(Options.CONNECT_TIMEOUT_EXPIRE), TimeUnit.MILLISECONDS);
	}

	@Override
	public long getLastUseTime() {
		return lastUseTime;
	}

	@Override
	public boolean isExpired(long idletime, TimeUnit tunit) {
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
