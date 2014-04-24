package com.tmall.search.httpclient.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.tmall.search.httpclient.client.HttpRequest.MethodName;
import com.tmall.search.httpclient.conn.ChunkContentPaser;
import com.tmall.search.httpclient.conn.ContentPaser;
import com.tmall.search.httpclient.conn.DefaultContentPaser;
import com.tmall.search.httpclient.conn.HttpConnection;
import com.tmall.search.httpclient.conn.HttpConnectiongManager;
import com.tmall.search.httpclient.params.HttpMethodParams;
import com.tmall.search.httpclient.util.CookieUtils;
import com.tmall.search.httpclient.util.HttpException;
import com.tmall.search.httpclient.util.HttpStatus;
import com.tmall.search.httpclient.util.RequestException;

/**
 * 
 * @author xiaolin.mxl
 */
public final class RequestDirector {

	private HttpConnectiongManager manager;
	private HttpRequest request;
	private HttpResponse httpResponse;
	private static final Logger LOG = LogManager.getLogger(RequestDirector.class);
	private HttpConnection conn;
	private Set<ClientCookie> setCookies = new HashSet<>();
	private HttpMethodParams httpMethodParams;

	public RequestDirector(HttpConnectiongManager manager, HttpRequest resq) {
		this.manager = manager;
		this.request = resq;
		this.httpMethodParams = resq.getHttpMethodParams();
	}

	/**
	 * cookie 处理逻辑不完整
	 * @return
	 * @throws HttpException
	 */
	public HttpResponse execute() throws HttpException,IOException {
		httpResponse = getResponse();
		if(httpMethodParams.isFollowRedirect()){
			processRedirectResponse();
		}
		return httpResponse;
	}
	
	/**
	 * 处理30X跳转
	 * @param resp
	 * @return
	 */
	private void processRedirectResponse() throws RedirectException{
		if(isRedirected()){
			int redirectNum = 0;
			while (redirectNum < 10 && isRedirected()) {
				redirectNum++;
				String locationHeader = httpResponse.getHeader().getHeaderElement(Header.LOCATION);
				if (locationHeader == null) {
					throw new RedirectException("Received redirect response " + httpResponse.getStatusCode() + " but no location header");
		        }
				List<String> cookies = httpResponse.getHeader().getHeaderElementsMap().get(Header.SET_COOKIE);
				try {
					request = new HttpRequest(locationHeader, MethodName.GET, httpMethodParams); //reset HttpRequest
					Set<ClientCookie> matchCookie = CookieUtils.match(CookieUtils.cookiePaser(cookies), request);
					if(matchCookie!=null){
						setCookies.addAll(matchCookie);
					}
					request.setCookies(setCookies);
					LOG.debug("Redirect:" + new String(request.getOutputDate()));
					httpResponse = getResponse();
				} catch (Exception e) {
					throw new RedirectException("URISyntaxException:" + locationHeader, e);
				}
			}
		}
	}
	
	//
	private static final String[] REDIRECT_METHODS = new String[] {
        MethodName.GET.toString(),
        MethodName.HEAD.toString()
    };
	
	/**
	 * 当前请求的方法是否允许跳转.
	 * @param method
	 * @return
	 */
	protected boolean isRedirectable(final String method) {
        for (final String m: REDIRECT_METHODS) {
            if (m.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
	}
	
	/**
	 * 是否需要跳转.
	 * @return
	 */
	public boolean isRedirected(){
        final int statusCode = httpResponse.getStatusCode();
        final String locationHeader = httpResponse.getHeader().getHeaderElement(Header.CONTENT_LEN);
        final String method = request.getMethodName().toString();
        switch (statusCode) {
        case HttpStatus.SC_MOVED_TEMPORARILY:
            return isRedirectable(method) && locationHeader != null;
        case HttpStatus.SC_MOVED_PERMANENTLY:
        case HttpStatus.SC_TEMPORARY_REDIRECT:
            return isRedirectable(method);
        case HttpStatus.SC_SEE_OTHER:
            return true;
        default:
            return false;
        } //end of switch
    }

	/**
	 * 一次http请求
	 * @return
	 * @throws HttpException
	 */
	private HttpResponse getResponse() throws HttpException {
		conn = manager.getConnectionWithTimeout(request.getHostInfo());
		HttpResponse hr = null;
		Header header = null;
		ByteBuffer buffer;
		try {
			buffer = executeWithRetry();
		} catch (RequestException e1) {
			manager.freeConnection(request.getHostInfo(), conn);
			throw new HttpException("assemble request error",e1);
		}
		try {
			header = new Header(buffer);
			ContentPaser paser;
			byte[] bodyData;
			if (header.isChunk()) {
				paser = new ChunkContentPaser(conn, header, buffer);
			} else {
				paser = new DefaultContentPaser(conn, header, buffer);
			}
			bodyData = paser.paser();
			if (header.isCompressed()) {
				bodyData = uncompress(bodyData);
			}
			hr = new HttpResponse(header, bodyData);
			if (hr.isClosed()) {
				manager.deleteConnection(request.getHostInfo(), conn);
				LOG.debug(request.getHostInfo() + " server closed this connection");
			} else {
				manager.freeConnection(request.getHostInfo(), conn);
			}
		} catch (HttpException | IOException e) {
			manager.deleteConnection(request.getHostInfo(), conn);
			throw new HttpException("Read Response error", e);
		}
		return hr;
	}
	
	
	private ByteBuffer executeWithRetry()throws HttpException,RequestException{
		int retryNum = 0;
		ByteBuffer buffer = null;
		byte[] rd = null;
		try {
			rd = request.getOutputDate();
		} catch (UnsupportedEncodingException e1) {
			throw new RequestException("assemble request error" , e1);
		}
		while (true) {
			retryNum++;
			if (retryNum > 2) {
				throw new HttpException("Maximum retries on connection failure !");
			}
			try {
				conn.sendRequest(rd);
				buffer = conn.read();
				if (buffer != null) {
					break;
				}
			} catch (HttpException e) {
				manager.deleteConnection(request.getHostInfo(), conn);
				conn = manager.getConnectionWithTimeout(request.getHostInfo());
				LOG.warn("Connection read failed after " + retryNum + " retries !", e);
			}
		}
		return buffer;
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public byte[] uncompress(byte[] data) throws IOException {
		byte[] result = null;
		try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			GZIPInputStream gZIPInputStream = new GZIPInputStream(byteArrayInputStream);) {
			result = IOUtils.toByteArray(gZIPInputStream);
			byteArrayInputStream.close();
		}
		return result;
	}

	
}
