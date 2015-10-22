package org.vadel.yandexdisk.authorization;

import org.vadel.common.Helpers;

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
		return "Basic " + Helpers.encode((login + ":" + pass).getBytes());//Base64Coder.encode(login + ":" + pass);;
	}
}
