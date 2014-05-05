package com.tmall.search.httpclient.conn;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.tmall.search.httpclient.params.ConnManagerParams;
import com.tmall.search.httpclient.params.ConnManagerParams.ConnImpl;
import com.tmall.search.httpclient.params.ConnManagerParams.Options;
import com.tmall.search.httpclient.util.HttpException;

public class MultiThreadConnectionManager implements HttpConnectiongManager {

	private static final Logger LOG = LogManager.getLogger(MultiThreadConnectionManager.class);
	//链接的管理池
	private final ConcurrentHashMap<HttpHost, HostConnectionQueue> connetionPool;
	private ConnManagerParams connParam;
	private final AtomicInteger globalConnNum = new AtomicInteger();
	private final AtomicInteger activeConnNum = new AtomicInteger();
	private final Semaphore connControl ;
	//关闭标识
	private volatile boolean shutdown = false;
	private Lock gcLock = new ReentrantLock();
	/**
	 * 构造线程安全的链接管理器.
	 * @param connParam.
	 * @param hostNum	concurrent的分块数量,影响到并发性能,根据多少ip来设置.
	 */
	public MultiThreadConnectionManager(final ConnManagerParams connParam) {
		if (connParam == null) {
			this.connParam = new ConnManagerParams();
		} else {
			this.connParam = connParam;
		}
		connetionPool = new ConcurrentHashMap<>(this.connParam.getMaxGlobalConnNum()<<1, 0.8F,64);
		connControl = new Semaphore(this.connParam.getMaxGlobalConnNum());
	}

	public MultiThreadConnectionManager() {
		this(null);
	}
	
	/**
	 * @throws HttpException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	@Override
	public HttpConnection getConnectionWithTimeout(HttpHost host) throws HttpException {
		HttpConnection connection = null;
		HostConnectionQueue hostQueue = getHostQueue(host,true);
		if (shutdown) {
			throw new HttpException("Connection factory has been shutdown.");
		}
		if ((connection = getFreeConnection(hostQueue)) != null) {
			return connection;
		}
	    if (hostQueue.liveConnNum.get() < this.connParam.getPerHostConnNum()) {
			if(!connControl.tryAcquire()){
				deleteLeastUsedConnection();
			}
			connection = createConnection(host,hostQueue);
		}else {
			try {
				connection = hostQueue.connQueue.poll(this.connParam.getValue(Options.GET_CONN_WAIT_TIMEOUT), TimeUnit.MILLISECONDS);
				if(connection==null){
					throw new HttpException("waiting a free connection timeout - " + host);
				}
				activeConnNum.incrementAndGet();
			} catch (InterruptedException e) {
				throw new HttpException("Unable to get a free connection from Queue - " + host, e);
			}
		}
		return connection;
	}

	private HttpConnection createConnection(HttpHost host , HostConnectionQueue hostQueue) throws HttpException{
		HttpConnection connection = null;
		if("https".equalsIgnoreCase(host.getProtocol())){
			connection = new SSLProtocolConnectionImpl(host, connParam);
		}else if(connParam.getConnImpl()==ConnImpl.AIO){
			connection = new ASyncConnectionImpl(host, connParam);
		}else if(connParam.getConnImpl()==ConnImpl.NIO){
			connection = new NIOConnectionImpl(host, connParam);
		}else{
			throw new HttpException("invoke unknow implemente" + connParam.getConnImpl());
		}
		int acn = activeConnNum.incrementAndGet();
		int liveNum = hostQueue.liveConnNum.incrementAndGet();
		int gNum = globalConnNum.incrementAndGet();
		LOG.info("Create New Connection for ["+ host.toString() + "]{connNum:" + liveNum + "}  " + gNum + ":" + acn +"="+ connection.getRemoteAddress());
		return connection;
	}
	
	
	private HostConnectionQueue getHostQueue(HttpHost host){
		return this.getHostQueue(host, false);
	}
	
	/**
	 * host单例
	 * @param host
	 * @param create 是否创建,默认写死创建.
	 * @return
	 */
	private HostConnectionQueue getHostQueue(HttpHost host, boolean create) {
		HostConnectionQueue connectionQueue = null;
		boolean createEntry = false;
		if(create){
			gcLock.lock();
			try{
				connectionQueue = connetionPool.get(host);
				if (connectionQueue == null) {
					connectionQueue = new HostConnectionQueue(this.connParam);
					HostConnectionQueue value = connetionPool.putIfAbsent(host, connectionQueue);
					if (value != null) {
						connectionQueue = value;
					}else{
						createEntry = true;
					}
				}
			}finally{
				gcLock.unlock();
			}
			if(createEntry && connetionPool.size()>this.connParam.getMaxGlobalConnNum()){
				closeIdleConnections();
			}
		}else{
			connectionQueue = connetionPool.get(host);
		}
		return connectionQueue;
	}

	/**
	 * 得到空闲conn,判断链接是否过期,如果过期,继续取得下一个链接.
	 * @param hostQueue host对应的链接队列
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private HttpConnection getFreeConnection(HostConnectionQueue hostQueue){
		HttpConnection connection = null;
		while ((connection = hostQueue.connQueue.poll()) != null) {
			if (!connection.isExpired()) {//判断是否过期
				activeConnNum.incrementAndGet();
				return connection;
			} else {
				deleteConnection(hostQueue, connection, "getFreeConnection : close Expired conn",false);
			}
		}
		return connection;
	}
	
	/**
	 * 
	 * @param hostQueue
	 * @param connection
	 * @param message
	 * @param isActive
	 * @return
	 */
	private boolean deleteConnection(HostConnectionQueue hostQueue, HttpConnection connection ,String message, boolean isActive) {
		boolean success = false;
		String remoteAddress = null;
		if (connection != null) {
			remoteAddress = connection.getRemoteAddress();
			try {
				connection.close();
				success = true;
			} catch (IOException e) {
				LOG.error("Connection close failure. - " + connection.getRemoteAddress() , e);
			}
		}else{
			LOG.error("can't close null connection");
		}
		int liveNum = hostQueue.liveConnNum.decrementAndGet();
		int gNum = globalConnNum.decrementAndGet();
		connControl.release();
		int acn = 0;
		if(isActive){
			acn = activeConnNum.decrementAndGet();
		}else{
			acn = activeConnNum.get();
		}
		LOG.info(message + " - Removed Connection [" + remoteAddress + "]{connNum:" + liveNum + "} "+ gNum+":"+acn);
		return success;
	}

	/**
	 * 删除链接,链接过期
	 */
	@Override
	public void deleteConnection(HttpHost host, HttpConnection connection,String message) {
		if (host != null && connection != null) {
			HostConnectionQueue hostQueue = getHostQueue(host);
			this.deleteConnection(hostQueue, connection, message, true);
		}else{
			LOG.error("deleteConnection error ! host=" + host + " connection=" + connection);
		}
	}

	/**
	 * 释放一个链接,这个链接重新加入到对应host的队列中,继续使用
	 */
	@Override
	public void freeConnection(HttpHost host, HttpConnection conn) {
		HostConnectionQueue hostQueue = getHostQueue(host);
		hostQueue.addConn(conn);
		activeConnNum.decrementAndGet();
	}

	@Override
	public ConnManagerParams getParams() {
		return connParam;
	}

	
	private void deleteLeastUsedConnection(){
		PriorityQueue<HostConnectionQueue> pq = new PriorityQueue<HostConnectionQueue>(connetionPool.size());
		for (Entry<HttpHost, HostConnectionQueue> entry : connetionPool.entrySet()) {
			pq.add(entry.getValue());
		}
		HostConnectionQueue cursor;
		boolean removeLeast = false;
		while ((cursor = pq.poll()) != null) {
			HttpConnection conn = null;
			conn = cursor.connQueue.poll();
			if (conn != null) {
				deleteConnection(cursor,conn,"deleteLeastUsedConnection",false);
				removeLeast = true;
				break;
			}
		}
		if(!removeLeast){
			LOG.warn("Can't remove any connection");
		}
	}
	
	/**
	 * TODO ...
	 */
	@Override
	public synchronized int closeIdleConnections() {
		int clearNum = 0;
		gcLock.lock();
		long curTime = System.currentTimeMillis();
		boolean timeOut = false;
		while(activeConnNum.get()!=0){
			if(System.currentTimeMillis()-curTime>this.connParam.getValue(Options.GET_CONN_WAIT_TIMEOUT)){
				timeOut = true;
				break;
			}
		}
		try{
			if(!timeOut){
				for (Entry<HttpHost, HostConnectionQueue> entry : connetionPool.entrySet()) {
					HostConnectionQueue queue = entry.getValue();
					int removeNum = queue.clearExpired();
					if(removeNum>0){
						globalConnNum.addAndGet(-removeNum);
						clearNum += removeNum;
					}
					if (queue.liveConnNum.get() == 0 && !queue.isActive()) {
						connetionPool.remove(entry.getKey());
						LOG.warn("httpclientwarn remove-----"+entry.getKey() + "-----------------------------------------" + entry.getValue());
					}
				}
			}
		}finally{
			gcLock.unlock();
		}
		LOG.warn("httpclientwarn Remove idle conn num :" + clearNum);
		return clearNum;
	}
	/**
	 * 关闭
	 */
	@Override
	public void shutDown() throws IOException{
		shutdown = true;
		long curTime = System.currentTimeMillis();
		while(activeConnNum.get()!=0){
			if(System.currentTimeMillis()-curTime>this.connParam.getValue(Options.GET_CONN_WAIT_TIMEOUT)){
				throw new IOException("Waiting for closing all the connection timeout");
			}
		}
		for (Entry<HttpHost, HostConnectionQueue> entry : connetionPool.entrySet()) {
			LOG.warn("httpclientwarn Ready to shut down ["+entry.getKey().toString() + "]{connNum: " + entry.getValue().connQueue.size()+"}");
			HostConnectionQueue hcq = entry.getValue();
			if(hcq.liveConnNum.get()!=hcq.connQueue.size()){
				LOG.warn("release conn timeout, conn leaked");
			}
			for (HttpConnection conn : hcq.connQueue) {
				deleteConnection(hcq,conn,"\tshutdown",false);
			}
		}
	}
	
	
	/**
	 * 每个host对应的链接队列
	 * @author xiaolin.mxl
	 */
	public static class HostConnectionQueue implements Comparable<HostConnectionQueue> {
		private ConnManagerParams connParam;
		public HostConnectionQueue(ConnManagerParams connParam) {
			this.connParam = connParam;
		}
		private volatile long lastUseTime = System.currentTimeMillis();
		//每个host的链接队列
		private LinkedTransferQueue<HttpConnection> connQueue = new LinkedTransferQueue<HttpConnection>();
		//这个host 链接在 数量
		private AtomicInteger liveConnNum = new AtomicInteger();

		public void addConn(HttpConnection conn) {
			connQueue.add(conn);
			lastUseTime = conn.getLastUseTime();
		}

		public boolean isActive(){
			return System.currentTimeMillis() - lastUseTime < connParam.getValue(Options.CONNECT_TIMEOUT_EXPIRE);
		}
		
		@Override
		public int compareTo(HostConnectionQueue o) {
			return Long.compare(lastUseTime, o.lastUseTime);
		}

		public int clearExpired(){
			int closeNum = 0;
			HttpConnection conn;
			while ((conn = connQueue.poll()) != null) {
				if (conn.isExpired(connParam.getValue(Options.CONNECT_TIMEOUT_EXPIRE), TimeUnit.MILLISECONDS)) {
					try {
						conn.close();
					} catch (IOException e) {
						LOG.error("Close Expired conn error", e);
					}
					liveConnNum.decrementAndGet();
					closeNum++;
				}else{
					connQueue.add(conn);
					break;
				}
			}
			return closeNum;
		}

	}
}
