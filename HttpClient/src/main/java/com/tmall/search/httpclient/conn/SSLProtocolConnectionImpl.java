package com.tmall.search.httpclient.conn;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.tmall.search.httpclient.params.ConnManagerParams;
import com.tmall.search.httpclient.params.ConnManagerParams.Options;
import com.tmall.search.httpclient.util.HttpException;

public class SSLProtocolConnectionImpl implements HttpConnection {
	public static final Logger LOG = LogManager.getLogger(SSLProtocolConnectionImpl.class);
	private Socket sslSocket;
	private ConnManagerParams connParams;
	private HttpHost host;
	private long lastUseTime = Long.MAX_VALUE;
	private final ByteBuffer readbuffer;
	private byte[] readArray;
	private DataOutputStream doStream;
	private DataInputStream inStream;
	
	public SSLProtocolConnectionImpl(HttpHost host, ConnManagerParams connParams) throws HttpException{
		this.connParams = connParams;
		this.host = host;
		try {
		sslSocket = SSLProtocolSocketFactory.getSocketFactory().createSocket(host.getHost(), 443);
		sslSocket.setKeepAlive(true);
		sslSocket.setSoTimeout(this.connParams.getValue(Options.READER_TIMROUT));
		inStream = new DataInputStream(sslSocket.getInputStream());
		doStream = new DataOutputStream(sslSocket.getOutputStream());
		} catch (IOException e) {
			throw new HttpException("构建失败.", e);
		}
		readArray = new byte[this.connParams.getValue(Options.SO_RCVBUF)];
		readbuffer = ByteBuffer.allocate(this.connParams.getValue(Options.SO_RCVBUF));
	}

	@Override
	public void sendRequest(byte[] requestData) throws HttpException {
		try {
			//LOG.debug("request : " + new String(requestData));
			lastUseTime = System.currentTimeMillis();//设置过期检测时间
			doStream.write(requestData);
			doStream.flush();
		} catch (IOException e) {
			throw new HttpException("Failure to send data.", e);
		}
	}

	@Override
	public void close() throws IOException {
		doStream.close();
		inStream.close();
		sslSocket.close();
	}

	@Override
	public ByteBuffer read() throws HttpException {
		readbuffer.clear();
		try {
			int arrayLength = inStream.read(readArray);
			if(arrayLength>0){
				readbuffer.put(readArray, 0, arrayLength);
			}
		} catch (IOException e) {
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
		return host.toString();
	}

}
