package org.vadel.common.yandexdisk;

import java.net.URI;

import org.apache.http.client.methods.HttpPost;

public class WebDavRequest extends HttpPost {
	
	String method = "POST"; 
	
	public WebDavRequest(String uri, String method) {
		super(uri);
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
	
}