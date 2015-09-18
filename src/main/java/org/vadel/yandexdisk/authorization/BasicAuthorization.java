package org.vadel.yandexdisk.authorization;

import org.vadel.common.Base64;

public class BasicAuthorization extends Authorization {

	public String login, pass;
	
	public BasicAuthorization(String login, String pass) {
		this.login = login;
		this.pass = pass;
	}

	@Override
	public boolean isValid() {
		return login != null && pass != null;
	}
	
	@Override
	public String getAuthorizationHeader() {
		return "Basic " + Base64.encode((login + ":" + pass).getBytes());//Base64Coder.encode(login + ":" + pass);;
	}
}
