package org.vadel.yandexdisk.authorization;

public class OAuthAuthorization extends Authorization {

	public String token;
	
	public OAuthAuthorization(String token) {
		this.token = token;
	}
	
	@Override
	public boolean isValid() {
		return token != null;
	}

	@Override
	public String getAuthorizationHeader() {
		return "OAuth " + token;
	}
}
