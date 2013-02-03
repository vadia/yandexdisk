package org.vadel.yandexdisk.authorization;

public abstract class Authorization {

	public abstract boolean isValid();
	
	public abstract String getAuthorizationHeader();
}
