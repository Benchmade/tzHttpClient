package com.tmall.search.httpclient.conn;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.tmall.search.httpclient.params.ConnManagerParams;
import com.tmall.search.httpclient.params.ConnManagerParams.Options;
import com.tmall.search.httpclient.util.HttpException;

public class ThreadSafeConnectionManager implements HttpConnectiongManager {

	private static final Logger LOG = LogManager.getLogger(ThreadSafeConnectionManager.class);
	//链接的管理池
	private final ConcurrentHashMap<HttpHost, HostConnectionQueue> connetionPool;
	private ConnManagerParams connParam;
	private final AtomicInteger globalConnNum = new AtomicInteger();
	//关闭标识
	private volatile boolean shutdown = false;
	private ReentrantLock poolEntryLock = new ReentrantLock();

	/**
	 * 构造线程安全的链接管理器.
	 * @param connParam.
	 * @param hostNum	concurrent的分块数量,影响到并发性能,根据多少ip来设置.
	 */
	public ThreadSafeConnectionManager(ConnManagerParams connParam, int hostNum) {
		if (connParam == null) {
			this.connParam = new ConnManagerParams();
		} else {
			this.connParam = connParam;
		}
		connetionPool = new ConcurrentHashMap<>(this.connParam.getValue(Options.CONN_MAX_NUM_PER_HOST), 0.8F, hostNum);
	}

	public ThreadSafeConnectionManager() {
		this(null, 128);
	}
	
	public ThreadSafeConnectionManager(ConnManagerParams connParam) {
		this(connParam, 128);
	}

	/**
	 * @throws HttpException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	@Override
	public HttpConnection getConnectionWithTimeout(HttpHost host) throws HttpException {
		HttpConnection connection = null;
		HostConnectionQueue hostQueue = getHostQueue(host, true);
		if (shutdown) {
			throw new HttpException("Connection factory has been shutdown.");
		}
		if ((connection = getFreeConnection(hostQueue)) != null) {
			return connection;
		}
		if (hostQueue.liveConnNum.intValue() < this.connParam.getValue(Options.CONN_MAX_NUM_PER_HOST)) {
			if (globalConnNum.intValue() >= this.connParam.getValue(Options.MAX_GLOBAL_CONN)) {
				LOG.warn("deleteLeastUsedConnection:" + globalConnNum.intValue());
				deleteLeastUsedConnection();
			}
			connection = new ASyncConnectionImpl(host, connParam);
			hostQueue.liveConnNum.incrementAndGet();
			globalConnNum.incrementAndGet();
			LOG.debug("Create New Connection for [" + host.toString() + "]{connNum:" + hostQueue.liveConnNum.intValue() + "}");
		} else {
			try {
				LOG.debug(host + " Waiting for the free connection");
				connection = hostQueue.connQueue.poll(this.connParam.getValue(Options.GET_CONN_WAIT_TIMEOUT), TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new HttpException("Unable to get a free connection from Queue", e);
			}
		}
		return connection;
	}

	/**
	 * host单例
	 * @param host
	 * @param create 是否创建,默认写死创建.
	 * @return
	 */
	private HostConnectionQueue getHostQueue(HttpHost host, boolean create) {
		/*HostConnectionQueue connections = connetionPool.get(host);
		if (connections == null) {
			connections = new HostConnectionQueue();
			HostConnectionQueue value = connetionPool.putIfAbsent(host, connections);
			if (value != null) {
				connections = value;
			}
		}*/
		HostConnectionQueue connections;
		poolEntryLock.lock();
		try {
			connections = connetionPool.get(host);
			if (connections == null) {
				connections = new HostConnectionQueue();
				connetionPool.put(host, connections);
			}
		} finally {
			poolEntryLock.unlock();
		}
		return connections;
	}

	/**
	 * TODO ...
	 */
	@Override
	public int closeIdleConnections(long idletime, TimeUnit tunit) {
		return connectionPoolGC(idletime, tunit);
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
				return connection;
			} else {
				deleteConnection(hostQueue, connection);
			}
		}
		return connection;
	}

	private boolean deleteConnection(HostConnectionQueue hostQueue, HttpConnection connection) {
		boolean success = false;
		if (connection != null) {
			try {
				LOG.debug("Removed Connection [" + connection.getRemoteAddress() + "]{connNum:" + hostQueue.liveConnNum + "}");
				connection.close();
				success = true;
			} catch (IOException e) {
				LOG.error("Connection close failure. - " + connection.getRemoteAddress() , e);
			}
		}
		hostQueue.liveConnNum.decrementAndGet();
		globalConnNum.decrementAndGet();
		return success;
	}

	/**
	 * 删除链接,链接过期
	 */
	@Override
	public void deleteConnection(HttpHost host, HttpConnection connection) {
		if (host != null && connection != null) {
			HostConnectionQueue hostQueue = getHostQueue(host, true);
			this.deleteConnection(hostQueue, connection);
		}else{
			LOG.error("deleteConnection error ! host=" + host + " connection=" + connection);
		}
	}

	/**
	 * 关闭
	 */
	@Override
	public void shutDown() throws IOException{
		shutdown = true;
		for (Entry<HttpHost, HostConnectionQueue> entry : connetionPool.entrySet()) {
			LOG.debug("Ready to shut down ["+entry.getKey().toString() + "]{connNum: " + entry.getValue().connQueue.size()+"}");
			HostConnectionQueue hcq = entry.getValue();
			if(hcq.liveConnNum.get()!=hcq.connQueue.size()){
				try {
					//等待时间在读+写超时内.
					int sleepTime = (this.connParam.getValue(Options.READER_TIMROUT)+this.connParam.getValue(Options.WRITE_TIMEOUT));
					LOG.info("Wait for the release using conn : "+sleepTime+"ms");
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					LOG.error(e);
				}
			}
			if(hcq.liveConnNum.get()!=hcq.connQueue.size()){
				LOG.warn("release conn timeout, conn leaked");
			}
			for (HttpConnection conn : hcq.connQueue) {
				deleteConnection(hcq,conn);
			}
		}
	}

	/**
	 * 释放一个链接,这个链接重新加入到对应host的队列中,继续使用
	 */
	@Override
	public void freeConnection(HttpHost host, HttpConnection conn) {
		HostConnectionQueue hostQueue = getHostQueue(host, true);
		hostQueue.addConn(conn);
	}

	@Override
	public ConnManagerParams getParams() {
		return connParam;
	}

	/**
	 * Stop the world
	 * @throws HttpException
	 */
	private int connectionPoolGC(long idletime, TimeUnit tunit){
		poolEntryLock.lock();
		LOG.info("begin reclaim an unused connection...");
		int clearNum = 0;
		try {
			for (Entry<HttpHost, HostConnectionQueue> entry : connetionPool.entrySet()) {
				int removeNum = entry.getValue().clearExpired(idletime, tunit);
				LOG.debug(entry.getClass().toString() + ":live conn" + entry.getValue().liveConnNum + " clear expired conn " + removeNum);
				globalConnNum.addAndGet(-removeNum);
				clearNum += removeNum;
				if (entry.getValue().liveConnNum.get() == 0) {
					connetionPool.remove(entry.getKey());
				}
			}
		} finally {
			poolEntryLock.unlock();
		}
		LOG.info("remove unused conn num :" + clearNum);
		return clearNum;
	}

	
	private void deleteLeastUsedConnection() throws HttpException {
		int clearNum = this.connectionPoolGC(this.connParam.getValue(Options.CONNECT_TIMEOUT_EXPIRE), TimeUnit.MILLISECONDS);
		if (clearNum == 0) {
			PriorityQueue<HostConnectionQueue> pq = new PriorityQueue<HostConnectionQueue>(connetionPool.size());
			for (Entry<HttpHost, HostConnectionQueue> entry : connetionPool.entrySet()) {
				pq.add(entry.getValue());
			}
			HostConnectionQueue cursor;
			boolean removeLeast = false;
			while ((cursor = pq.poll()) != null) {
				HttpConnection conn = cursor.connQueue.poll();
				if (conn != null) {
					deleteConnection(cursor,conn);
					removeLeast = true;
					break;
				}
			}
			if(!removeLeast){
				throw new HttpException("Can't remove any connection, connPool is full");
			}
		}
	}

	/**
	 * 每个host对应的链接队列
	 * @author xiaolin.mxl
	 */
	public static class HostConnectionQueue implements Comparable<HostConnectionQueue> {
		private volatile long lastUseTime = System.currentTimeMillis();
		//每个host的链接队列
		private LinkedTransferQueue<HttpConnection> connQueue = new LinkedTransferQueue<HttpConnection>();
		//这个host 链接在 数量
		private AtomicInteger liveConnNum = new AtomicInteger();

		public void addConn(HttpConnection conn) {
			connQueue.add(conn);
			lastUseTime = conn.getLastUseTime();
		}

		@Override
		public int compareTo(HostConnectionQueue o) {
			return Long.compare(lastUseTime, o.lastUseTime);
		}

		public int clearExpired(long idletime, TimeUnit tunit){
			int closeNum = 0;
			HttpConnection conn;
			while ((conn = connQueue.poll()) != null) {
				if (conn.isExpired(idletime, tunit)) {
					try {
						conn.close();
					} catch (IOException e) {
						LOG.error("Close Expired conn error", e);
					}
					liveConnNum.decrementAndGet();
					closeNum++;
				}
			}
			return closeNum;
		}

	}

}
