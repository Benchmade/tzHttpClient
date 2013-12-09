package com.tmall.search.httpclient.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.tmall.search.httpclient.compress.AcceptDecoder;
import com.tmall.search.httpclient.compress.GzipDecoder;
import com.tmall.search.httpclient.conn.ChunkContentPaser;
import com.tmall.search.httpclient.conn.ContentPaser;
import com.tmall.search.httpclient.conn.DefaultContentPaser;
import com.tmall.search.httpclient.conn.HttpConnection;
import com.tmall.search.httpclient.conn.HttpConnectiongManager;
import com.tmall.search.httpclient.util.HttpException;
import com.tmall.search.httpclient.util.HttpStatus;

/**
 * 
 * @author xiaolin.mxl
 */
public final class RequestDirector {

	private HttpConnectiongManager manager;
	private HttpRequest resq;
	private static final Logger LOG = LogManager.getLogger(RequestDirector.class);
	private HttpConnection conn;

	public RequestDirector(HttpConnectiongManager manager, HttpRequest resq) {
		this.manager = manager;
		this.resq = resq;
	}

	/**
	 * cookie 处理逻辑不完整
	 * @return
	 * @throws HttpException
	 */
	public HttpResponse execute() throws HttpException,IOException {
		HttpResponse resp = getResponse();
		/*int redirectNum = 0;
		while (redirectNum < 20 && isRedirectNeeded(resp)) {
			redirectNum++;
			String url = resp.getHeaderElements().get("Location");
			String cookie = resp.getHeaderElements().get("Set-Cookie");
			try {
				resq = new HttpRequest(url, MethodName.GET);
				resq.setCookie(cookie);
				resp = getResponse();
			} catch (URISyntaxException e) {
				throw new HttpException("URISyntaxException:" + url, e);
			}
		}*/
		return resp;
	}

	/**
	 * current conn requires a redirect to another location.
	 * @param resp
	 * @return
	 */
	@Deprecated
	private boolean isRedirectNeeded(final HttpResponse resp) {
		if (resp.getStatusCode() == HttpStatus.SC_OK) {
			return false;
		}
		switch (resp.getStatusCode()) {
		case HttpStatus.SC_MOVED_TEMPORARILY:
		case HttpStatus.SC_MOVED_PERMANENTLY:
		case HttpStatus.SC_SEE_OTHER:
		case HttpStatus.SC_TEMPORARY_REDIRECT:
			LOG.debug("Redirect required");
			if (resq.isFollowRedirects()) {
				return true;
			} else {
				return false;
			}
		default:
			return false;
		} //end of switch
	}

	/**
	 * 一次http请求
	 * @return
	 * @throws HttpException
	 */
	private HttpResponse getResponse() throws HttpException,IOException {
		int retryNum = 0;
		conn = manager.getConnectionWithTimeout(resq.getHost());
		HttpResponse hr = null;
		ByteBuffer buffer = null;
		Header header = null;
		while (true) {
			retryNum++;
			if (retryNum > 2) {
				throw new HttpException("Maximum retries on connection failure !");
			}
			try {
				conn.sendRequest(resq);
				buffer = conn.read();
				if (buffer != null) {
					break;
				}
			} catch (ExecutionException | TimeoutException | InterruptedException e) {
				manager.deleteConnection(resq.getHost(), conn);
				conn = manager.getConnectionWithTimeout(resq.getHost());
				LOG.info("Connection read failed after " + retryNum + " retries !", e);
			}
		}
		try {
			header = new Header(buffer);
			ContentPaser paser;
			if (header.isChunk()) {
				paser = new ChunkContentPaser(conn, header, buffer);
			} else {
				paser = new DefaultContentPaser(conn, header, buffer);
			}
			byte[] bodyData = paser.paser();
			if (header.isCompressed()) {
				//String compressAlgorithm = header.getHeaderElements().get(Header.CONTENT_ENCODING);
				AcceptDecoder ad = new GzipDecoder();//DecoderUtils.getAcceptDecoder(resq.getInOrderAcceptEncodingList(), compressAlgorithm);
				bodyData = ad.uncompress(bodyData);
			}
			hr = new HttpResponse(header, bodyData);
			if (hr.isClosed()) {
				manager.deleteConnection(resq.getHost(), conn);
				LOG.debug(resq.getHost() + " server closed this connection");
			} else {
				manager.freeConnection(resq.getHost(), conn);
			}
		} catch (HttpException | IOException e) {
			manager.deleteConnection(resq.getHost(), conn);
			throw new HttpException("Read Response error", e);
		}
		return hr;
	}
}
