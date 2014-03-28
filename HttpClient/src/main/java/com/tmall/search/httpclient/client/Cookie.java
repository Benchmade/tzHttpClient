package com.tmall.search.httpclient.client;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.util.LangUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Cookie implements Serializable, Comparator<Cookie> {

	private static final long serialVersionUID = 7619515513902572533L;
	/** Comment attribute. */
	private String cookieComment;
	/** Domain attribute. */
	private String cookieDomain;
	/** Expiration {@link Date}. */
	private Date cookieExpiryDate;
	/** Path attribute. */
	private String cookiePath;
	/** My secure flag. */
	private boolean isSecure;
	// Specifies if the set-cookie header included a Path attribute for this cookie
	private boolean hasPathAttribute = false;
	//Specifies if the set-cookie header included a Domain attribute for this cookie
	private boolean hasDomainAttribute = false;
	/** The version of the cookie specification I was created from. */
	private int cookieVersion = 0;
	/** Log object for this class */
	private static final Log LOG = LogFactory.getLog(Cookie.class);
	private String key;
	private String value;
	
	
	public Cookie(String domain, String key, String value, String path, Date expires, boolean secure) {

		LOG.trace("enter Cookie(String, String, String, String, Date, boolean)");
		if (key == null) {
			throw new IllegalArgumentException("Cookie name may not be null");
		}
		if (key.trim().equals("")) {
			throw new IllegalArgumentException("Cookie name may not be blank");
		}
		this.setPath(path);
		this.setDomain(domain);
		this.setExpiryDate(expires);
		this.setSecure(secure);
	}

	public Cookie(String domain, String name, String value, String path, int maxAge, boolean secure) {

		this(domain, name, value, path, null, secure);
		if (maxAge < -1) {
			throw new IllegalArgumentException("Invalid max age:  " + Integer.toString(maxAge));
		}
		if (maxAge >= 0) {
			setExpiryDate(new Date(System.currentTimeMillis() + maxAge * 1000L));
		}
	}

	public String getComment() {
		return cookieComment;
	}

	/**
	 * If a user agent (web browser) presents this cookie to a user, the cookie's purpose will be described using this comment.
	 * @param comment
	 * @see #getComment()
	 */
	public void setComment(String comment) {
		cookieComment = comment;
	}

	public Date getExpiryDate() {
		return cookieExpiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		cookieExpiryDate = expiryDate;
	}

	/**
	 * Returns <tt>false</tt> if the cookie should be discarded at the end of the "session"; <tt>true</tt> otherwise.
	 * @return <tt>false</tt> if the cookie should be discarded at the end of the "session"; <tt>true</tt> otherwise
	 */
	public boolean isPersistent() {
		return (null != cookieExpiryDate);
	}

	/**
	 * Returns domain attribute of the cookie.
	 * @return the value of the domain attribute
	 */
	public String getDomain() {
		return cookieDomain;
	}

	/**
	 * Sets the domain attribute.
	 * @param domain The value of the domain attribute
	 * @see #getDomain
	 */
	public void setDomain(String domain) {
		if (domain != null) {
			int ndx = domain.indexOf(":");
			if (ndx != -1) {
				domain = domain.substring(0, ndx);
			}
			cookieDomain = domain.toLowerCase();
		}
	}

	/**
	 * Returns the path attribute of the cookie
	 * @return The value of the path attribute.
	 * @see #setPath(java.lang.String)
	 */
	public String getPath() {
		return cookiePath;
	}

	/**
	 * Sets the path attribute.
	 * @param path The value of the path attribute
	 * @see #getPath
	 */
	public void setPath(String path) {
		cookiePath = path;
	}

	/** @return <code>true</code> if this cookie should only be sent over secure connections.
	 * @see #setSecure(boolean)
	 */
	public boolean getSecure() {
		return isSecure;
	}

	/**
	 * Sets the secure attribute of the cookie.
	 * <p>
	 * When <tt>true</tt> the cookie should only be sent
	 * using a secure protocol (https).  This should only be set when
	 * the cookie's originating server used a secure protocol to set the
	 * cookie's value.
	 *
	 * @param secure The value of the secure attribute
	 * 
	 * @see #getSecure()
	 */
	public void setSecure(boolean secure) {
		isSecure = secure;
	}

	/**
	 * Returns the version of the cookie specification to which this
	 * cookie conforms.
	 *
	 * @return the version of the cookie.
	 * 
	 * @see #setVersion(int)
	 *
	 */
	public int getVersion() {
		return cookieVersion;
	}

	/**
	 * Sets the version of the cookie specification to which this
	 * cookie conforms. 
	 *
	 * @param version the version of the cookie.
	 * 
	 * @see #getVersion
	 */
	public void setVersion(int version) {
		cookieVersion = version;
	}

	/**
	 * Returns true if this cookie has expired.
	 * 
	 * @return <tt>true</tt> if the cookie has expired.
	 */
	public boolean isExpired() {
		return (cookieExpiryDate != null && cookieExpiryDate.getTime() <= System.currentTimeMillis());
	}

	public boolean isExpired(Date now) {
		return (cookieExpiryDate != null && cookieExpiryDate.getTime() <= now.getTime());
	}

	/**
	 * Indicates whether the cookie had a path specified in a 
	 * path attribute of the <tt>Set-Cookie</tt> header. This value
	 * is important for generating the <tt>Cookie</tt> header because 
	 * some cookie specifications require that the <tt>Cookie</tt> header 
	 * should only include a path attribute if the cookie's path 
	 * was specified in the <tt>Set-Cookie</tt> header.
	 *
	 * @param value <tt>true</tt> if the cookie's path was explicitly 
	 * set, <tt>false</tt> otherwise.
	 * 
	 * @see #isPathAttributeSpecified
	 */
	public void setPathAttributeSpecified(boolean value) {
		hasPathAttribute = value;
	}

	/**
	 * Returns <tt>true</tt> if cookie's path was set via a path attribute
	 * in the <tt>Set-Cookie</tt> header.
	 * @return value <tt>true</tt> if the cookie's path was explicitly set, <tt>false</tt> otherwise.
	 * @see #setPathAttributeSpecified
	 */
	public boolean isPathAttributeSpecified() {
		return hasPathAttribute;
	}

	/**
	 * Indicates whether the cookie had a domain specified in a 
	 * domain attribute of the <tt>Set-Cookie</tt> header. This value
	 * is important for generating the <tt>Cookie</tt> header because 
	 * some cookie specifications require that the <tt>Cookie</tt> header 
	 * should only include a domain attribute if the cookie's domain 
	 * was specified in the <tt>Set-Cookie</tt> header.
	 *
	 * @param value <tt>true</tt> if the cookie's domain was explicitly 
	 * set, <tt>false</tt> otherwise.
	 *
	 * @see #isDomainAttributeSpecified
	 */
	public void setDomainAttributeSpecified(boolean value) {
		hasDomainAttribute = value;
	}

	/**
	 * Returns <tt>true</tt> if cookie's domain was set via a domain 
	 * attribute in the <tt>Set-Cookie</tt> header.
	 *
	 * @return value <tt>true</tt> if the cookie's domain was explicitly 
	 * set, <tt>false</tt> otherwise.
	 *
	 * @see #setDomainAttributeSpecified
	 */
	public boolean isDomainAttributeSpecified() {
		return hasDomainAttribute;
	}

	/**
	 * Returns a hash code in keeping with the
	 * {@link Object#hashCode} general hashCode contract.
	 * @return A hash code
	 */
	public int hashCode() {
		int hash = LangUtils.HASH_SEED;
		hash = LangUtils.hashCode(hash, this.key);
		hash = LangUtils.hashCode(hash, this.cookieDomain);
		hash = LangUtils.hashCode(hash, this.cookiePath);
		return hash;
	}

	/**
	 * Two cookies are equal if the name, path and domain match.
	 * @param obj The object to compare against.
	 * @return true if the two objects are equal.
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (obj instanceof Cookie) {
			Cookie that = (Cookie) obj;
			return LangUtils.equals(this.key, that.key) && LangUtils.equals(this.cookieDomain, that.cookieDomain)
					&& LangUtils.equals(this.cookiePath, that.cookiePath);
		} else {
			return false;
		}
	}


	public int compare(Cookie c1, Cookie c2) {
		LOG.trace("enter Cookie.compare(Object, Object)");

		if (c1.getPath() == null && c2.getPath() == null) {
			return 0;
		} else if (c1.getPath() == null) {
			// null is assumed to be "/"
			if (c2.getPath().equals(CookieSpec.PATH_DELIM)) {
				return 0;
			} else {
				return -1;
			}
		} else if (c2.getPath() == null) {
			// null is assumed to be "/"
			if (c1.getPath().equals(CookieSpec.PATH_DELIM)) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return c1.getPath().compareTo(c2.getPath());
		}
	}

}
