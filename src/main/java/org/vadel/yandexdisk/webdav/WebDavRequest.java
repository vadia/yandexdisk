package org.vadel.yandexdisk.webdav;

import org.apache.http.client.methods.HttpPost;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

@SuppressWarnings("unused")
public class WebDavRequest extends HttpPost {
	
	String method = "POST"; 
	
	public WebDavRequest(String uri, String method) throws UnsupportedEncodingException {
		super(getURLPathEscape(uri));
		this.method = method; 
	}
	
	public WebDavRequest(URI uri, String method) {
		super(uri);
		this.method = method; 
	}
	
	@Override
	public String getMethod() {
		return method;
	}
	
	public WebDavRequest setMethod(String value) {
		method = value;
		return this;
	}
	
	/**
	 * 
	 * @param u Url which need escape a path
	 * @return Url escaped path
	 * @throws UnsupportedEncodingException 
	 */
	public static String getURLPathEscape(String u) throws UnsupportedEncodingException {
		if (u.contains("%"))
			return u;
		int start = u.indexOf("//");
		if (start >= 0) 
			start += 2;
		else
			start = 0;
		start = u.indexOf('/', start);
		if (start < 0 || start == u.length() - 1) 
			return u;
		final int last;
		int i1 = u.lastIndexOf('?');
		int i2 = u.lastIndexOf('#');
		if (i1 >= 0 && i2 >= 0) 
			last = Math.min(i1, i2);
		else if (i1 >= 0)
			last = i1;
		else if (i2 >= 0)
			last = i2;
		else
			last = u.length();
		
		StringBuilder str = new StringBuilder(u.substring(0, start + 1));
		i1 = start;
		while (i1 >= 0 && i1 < last) {
			i2 = u.indexOf('/', i1 + 1);
			boolean isend = i2 < 0;
			String path;
			if (isend) 
				path = u.substring(i1 + 1, last);
			else
				path = u.substring(i1 + 1, i2);
			str.append(URLEncoder.encode(path, "utf-8").replace("+", "%20"));
			if (!isend)
				str.append('/');
			i1 = i2;
		}		
		if (last != u.length())
			str.append(u.substring(last));
		return str.toString();
	}
}