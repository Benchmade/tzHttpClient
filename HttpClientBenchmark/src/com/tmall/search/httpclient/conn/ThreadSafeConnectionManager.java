package com.tmall.search.httpclient.conn;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.tmall.search.httpclient.params.ConnManagerParams;
import com.tmall.search.httpclient.params.ConnManagerParams.Options;
import com.tmall.search.httpclient.util.HttpException;

public class ThreadSafeConnectionManager implements HttpConnectiongManager {

	private Logger LOG = LogManager.getLogger(ThreadSafeConnectionManager.class);
	//链接的管理池
	private final ConcurrentHashMap<HttpHost, HostConnectionQueue> pool;
	private ConnManagerParams connParam;
	//关闭标识
	private volatile boolean shutdown = false;

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
		pool = new ConcurrentHashMap<HttpHost, HostConnectionQueue>(this.connParam.getValue(Options.CONN_MAX_NUM_PER_HOST), 0.75F, hostNum);
	}

	public ThreadSafeConnectionManager() {
		this(null, 16);
	}

	/**
	 * @throws HttpException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	@Override
	public HttpConnection getConnectionWithTimeout(HttpHost host) throws HttpException, IOException {
		HttpConnection connection = null;
		HostConnectionQueue hostQueue = getHostQueue(host, true);
		if (shutdown) {
			throw new HttpException("Connection factory has been shutdown.");
		}
		if ((connection = getFreeConnection(hostQueue, host)) != null) {
			return connection;
		}
		if (hostQueue.liveConnNum.intValue() < this.connParam.getValue(Options.CONN_MAX_NUM_PER_HOST)) {//队列内大小和队列外大小和,小于限制数
			connection = new AIOConnectionImpl(host, connParam);
			hostQueue.liveConnNum.incrementAndGet();
			LOG.debug("Create New Connection for [" + host.toString() + "](" + hostQueue.liveConnNum.intValue() + ")");
		} else {//放弃blocking模式提高性能,但是没有好的方式实现blocking模式的实时通知.只能wait后再获得,期间可能有空闲被取走,nofair
			//Thread.sleep(this.connParam.getValue(Options.GET_CONN_WAIT_TIMEOUT));
			long timeOut = System.currentTimeMillis() + this.connParam.getValue(Options.GET_CONN_WAIT_TIMEOUT);
			while ((connection = hostQueue.connQueue.poll()) == null) {
				if (System.currentTimeMillis() > timeOut) {
					throw new HttpException("Failed to acquire connection: " + host.toString());
				}
				synchronized (hostQueue) {
					try {
						hostQueue.wait(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
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
		HostConnectionQueue connections = pool.get(host);
		if (connections == null) {
			connections = new HostConnectionQueue();
			HostConnectionQueue value = pool.putIfAbsent(host, connections);
			if (value != null) {
				connections = value;
			}
		}
		return connections;
	}

	@Override
	public void closeIdleConnections(long idletime, TimeUnit tunit) {
		long deadline = System.currentTimeMillis() - tunit.toMillis(idletime);
		boolean isOutOfBounds = this.connParam.getValue(Options.MAX_TOTAL_HOST) > pool.size();
		Enumeration<HttpHost> hosts = pool.keys();
		HttpHost host ;
		while (hosts.hasMoreElements()) {
			host = hosts.nextElement();
			HostConnectionQueue hcq = pool.get(host);
			hcq.clearIdle();
			if(hcq.liveConnNum.intValue()==0 && isOutOfBounds){
				pool.remove(host);
			}
		}
	}

	/**
	 * 得到空闲conn,判断链接是否过期,如果过期,继续取得下一个链接.
	 * @param hostQueue host对应的链接队列
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private HttpConnection getFreeConnection(HostConnectionQueue hostQueue, HttpHost host) throws IOException {
		HttpConnection connection = null;
		while ((connection = hostQueue.connQueue.poll()) != null) {
			if (!connection.isExpired()) {//判断是否过期
				return connection;
			} else {
				deleteConnection(hostQueue, connection, host);
			}
		}
		return connection;
	}

	private void deleteConnection(HostConnectionQueue hostQueue, HttpConnection connection, HttpHost host) throws IOException {
		if (connection != null) {
			connection.close();
		}
		hostQueue.liveConnNum.decrementAndGet();
		LOG.debug("Removed Connection [" + host.toString() + "](" + hostQueue.liveConnNum + ")");
	}

	/**
	 * 删除链接,链接过期
	 */
	@Override
	public void deleteConnection(HttpHost host, HttpConnection connection) throws IOException {
		HostConnectionQueue hostQueue = getHostQueue(host, true);
		this.deleteConnection(hostQueue, connection, host);
	}

	/**
	 * 关闭
	 */
	@Override
	public void shutDown() throws IOException {
		shutdown = true;
		for (Entry<HttpHost, HostConnectionQueue> entry : pool.entrySet()) {
			LOG.debug("#######################:" + entry.getValue().connQueue.size());
			for (HttpConnection conn : entry.getValue().connQueue) {
				conn.close();
			}
		}
	}

	/**
	 * 释放一个链接,这个链接重新加入到对应host的队列中,继续使用
	 */
	@Override
	public void freeConnection(HttpHost host, HttpConnection conn) {
		HostConnectionQueue hostQueue = getHostQueue(host, true);
		hostQueue.connQueue.add(conn);
	}

	@Override
	public void close(HttpHost host, HttpConnection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ConnManagerParams getParam() {
		return connParam;
	}
	
	
	/**
	 * 每个host对应的链接队列
	 * @author xiaolin.mxl
	 */
	private class HostConnectionQueue {
		//每个host的链接队列
		private ConcurrentLinkedQueue<HttpConnection> connQueue = new ConcurrentLinkedQueue<HttpConnection>();
		//这个host 链接在 数量
		private AtomicInteger liveConnNum = new AtomicInteger();
		
		public void clearIdle(){
			HttpConnection conn;
			while((conn=connQueue.poll())!=null){
				try {
					if(conn.isExpired()){
						conn.close();
						liveConnNum.decrementAndGet();
					}
				} catch (IOException e) {
					LOG.warn("conn clear ", e);
				}
			}
		}
	}

}
