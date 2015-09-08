package org.vadel.yandexdisk;

import org.apache.http.client.HttpResponseException;

public class YandexDiskException extends HttpResponseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5771308835736872588L;

	public YandexDiskException(int code, String s) {
		super(code, s);
	}
	
}
