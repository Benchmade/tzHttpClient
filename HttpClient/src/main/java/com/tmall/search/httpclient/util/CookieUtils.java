package com.tmall.search.httpclient.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import com.tmall.search.httpclient.client.ClientCookie;
import com.tmall.search.httpclient.client.HttpRequest;

public final class CookieUtils {

	/** Path delimiter */
    static final String PATH_DELIM = "/";

    /** Path delimiting charachter */
    static final char PATH_DELIM_CHAR = PATH_DELIM.charAt(0);
	
	/*
	Cookie:_tb_token_=igQNox9Oi2Ed;domain=.taobao.com;Path=/;HttpOnly
	Cookie:cookie2=daa210d90746c4b51ba8808efd63f652;domain=.taobao.com;Path=/;HttpOnly
	Cookie:t=beca55a65e7bbdbc4a04414ac41dfcc3;domain=.taobao.com;Expires=Sun, 29-Jun-2014 02:23:37 GMT;Path=/;Secure
	*/
	public static void main(String[] args) throws IllegalCookieException {
		String s = "t=beca55a65e7bbdbc4a04414ac41dfcc3;domain=.taobao.com;Expires=Sun, 29-Jun-2014 02:23:37 GMT;Path=/;Secure";
		//String s ="t=124;";
		ClientCookie c = cookiePaser(s);
		System.out.println(c);
	}
	
	public static boolean dateMatch(Date cookieDate){
		return cookieDate == null  || cookieDate.after(new Date());
	}

	public static Set<ClientCookie> match(List<ClientCookie> cookies, HttpRequest httpRequest) {
		Set<ClientCookie> newList = new HashSet<>(cookies.size());
		for (ClientCookie cookie : cookies) {
			if (domainMatch(httpRequest.getHostInfo().getHost(), cookie.getDomain()) && pathMatch(httpRequest.getPath(),cookie.getPath()) && dateMatch(cookie.getExpiryDate())) {
				newList.add(cookie);
			}
		}
		return newList;
	}
	
	
	public static boolean pathMatch(final String path, final String topmostPath) {
        boolean match = path.startsWith (topmostPath);
        // if there is a match and these values are not exactly the same we have
        // to make sure we're not matcing "/foobar" and "/foo"
        if (match && path.length() != topmostPath.length()) {
            if (!topmostPath.endsWith(PATH_DELIM)) {
                match = (path.charAt(topmostPath.length()) == PATH_DELIM_CHAR);
            }
        }
        return match;
    }

	public static boolean domainMatch(final String host, String domain) {
		if (host.equals(domain)) {
			return true;
		}
		if (!domain.startsWith(".")) {
			domain = "." + domain;
		}
		return host.endsWith(domain) || host.equals(domain.substring(1));
	}

	public static List<ClientCookie> cookiePaser(List<String> setCookie) throws IllegalCookieException {
		List<ClientCookie> list = new ArrayList<>();
		if(setCookie!=null && setCookie.size()>0){
			for (String cookieStr : setCookie) {
				ClientCookie cookie = cookiePaser(cookieStr);
				if(cookie!=null){
					list.add(cookie);
				}
			}
		}
		return list;
	}

	/**
	 * 
	 * @param setCookie header中获得的setCookie字段.
	 * @return	返回格式化后.
	 * @throws IllegalCookieException
	 */
	public static ClientCookie cookiePaser(String setCookie) throws IllegalCookieException {
		ClientCookie result = null;
		CookieCursor cursor = new CookieCursor();
		char[] charArray = setCookie.toCharArray();
		while (cursor.getPos() < charArray.length) {
			getCookieKey(charArray, cursor);
			String key = new String(charArray, cursor.getMark(), cursor.getStep());
			cursor.flip();
			String value = "";
			if (!cursor.isTerminated()) {
				getCookieValue(charArray, cursor);
				value = new String(charArray, cursor.getMark(), cursor.getStep());
				cursor.flip();
			}
			if (result == null) {
				result = new ClientCookie(key, value);
			} else {
				switch (key.toLowerCase().trim()) {
				case ClientCookie.DOMAIN_ATTR:
					result.setDomain(value);
					break;
				case ClientCookie.EXPIRES_ATTR:
					SimpleDateFormat format = new SimpleDateFormat(ClientCookie.PATTERN_RFC1036, Locale.US);
					format.setTimeZone(TimeZone.getTimeZone("GMT"));
					try {
						result.setExpiryDate(format.parse(value));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					break;
				case ClientCookie.PATH_ATTR:
					result.setPath(value);
					break;
				default:
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * @param charArray
	 * @param cursors
	 * @throws IllegalCookieException
	 */
	private static void getCookieKey(char[] charArray, CookieCursor cursors) throws IllegalCookieException {
		int cursor = cursors.getPos();
		while (cursor < charArray.length) {
			if (charArray[cursor] == '=') {
				cursors.setStep(cursor - cursors.getPos());
				cursors.setPos(cursor + 1);
				break;
			} else if (charArray[cursor] == ';') {
				throw new IllegalCookieException();
			}
			cursor++;
			if (cursor == charArray.length) {
				cursors.setStep(cursor - cursors.getPos());
				cursors.setPos(cursor);
				cursors.setTerminated(true);
				break;
			}
		}
	}

	/**
	 * 得到=号后面的value.
	 * @param charArray 元数据
	 * @param cursors	游标记录
	 * @throws IllegalCookieException
	 */
	private static void getCookieValue(char[] charArray, CookieCursor cursors) throws IllegalCookieException {
		int cursor = cursors.getPos();
		while (cursor < charArray.length) {
			if (charArray[cursor] == '=') {
				throw new IllegalCookieException();
			} else if (charArray[cursor] == ';') {
				cursors.setStep(cursor - cursors.getPos());
				cursors.setPos(cursor + 1);
				break;
			}
			cursor++;
			if (cursor == charArray.length) {
				cursors.setStep(cursor - cursors.getPos());
				cursors.setPos(cursor);
				break;
			}
		}
	}

	/**
	 * 
	 * @author xiaolin.mxl
	 */
	private static class CookieCursor {
		private int pos; //
		private int step; //
		private int mark; //起始位置
		boolean terminated = false;

		public int getPos() {
			return pos;
		}

		public void setPos(int pos) {
			this.pos = pos;
		}

		public int getStep() {
			return step;
		}

		public void setStep(int step) {
			this.step = step;
		}

		public int getMark() {
			return mark;
		}

		public void flip() {
			mark = pos;
		}

		public boolean isTerminated() {
			return terminated;
		}

		public void setTerminated(boolean terminated) {
			this.terminated = terminated;
		}
	}

}
