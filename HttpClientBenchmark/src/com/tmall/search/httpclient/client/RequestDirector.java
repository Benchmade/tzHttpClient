package com.tmall.search.httpclient.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.tmall.search.httpclient.conn.ChunkContentPaser;
import com.tmall.search.httpclient.conn.ContentPaser;
import com.tmall.search.httpclient.conn.DefaultContentPaser;
import com.tmall.search.httpclient.conn.HttpConnection;
import com.tmall.search.httpclient.conn.HttpConnectiongManager;
import com.tmall.search.httpclient.util.HttpException;

/**
 * 
 * @author xiaolin.mxl
 *
 */
public class RequestDirector {

	private HttpConnectiongManager manager;
    private HttpRequest resq;
    private final Logger LOG = LogManager.getLogger(RequestDirector.class);
    private HttpConnection conn;
	public RequestDirector(HttpConnectiongManager manager,HttpRequest resq) {
		this.manager = manager;
		this.resq = resq;
	}
	
	public HttpResponse execute() throws IOException {
		int execCount=0;
		conn = manager.getConnectionWithTimeout(resq.getHost());
		HttpResponse hr = null;
		ByteBuffer buffer=null;
		Header header=null;
		while(true){
			execCount++;
			if(execCount>2){
				throw new HttpException("Maximum retries on connection failure !");
			}
			try{
				conn.sendRequest(resq);
				buffer = conn.read();
				if(buffer!=null){
					break;
				}
			}catch(ExecutionException | TimeoutException | InterruptedException e){
				manager.deleteConnection(resq.getHost(),conn);
				conn = manager.getConnectionWithTimeout(resq.getHost());
				LOG.debug("Connection read failed after "+ execCount +" retries !");
			}
		}
		try {
			header = new Header(buffer);
			ContentPaser paser;
			if(header.isChunk()){
				paser = new ChunkContentPaser();
			}else{
				paser = new DefaultContentPaser();
			}
			byte[] bodyData = paser.paser(conn, header, buffer);
			hr = new HttpResponse(header, bodyData);
			if(hr.isClose()){
				manager.deleteConnection(resq.getHost(), conn);
				LOG.debug(resq.getHost() + " server closed this connection");
			}else{
				manager.freeConnection(resq.getHost(), conn);
			}
		} catch (IOException e) {
			manager.deleteConnection(resq.getHost(), conn);
			e.printStackTrace();
		}
		return hr;
	}
}
