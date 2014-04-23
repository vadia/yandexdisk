package org.vadel.googledrive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.vadel.common.Helpers;

public class GoogleDriveApi {

	private static final String QUERY_PARAM_CODE = "code=";
	
	static final String BASE_OAUTH_REDIRECT_URL = "https://accounts.google.com/o/oauth2/auth" +
			"?access_type=offline&approval_prompt=force" +
			"&client_id=%s&redirect_uri=%s&response_type=code" +
			"&scope=%s&state=google-flow&user_id";
	static final String OAUTH2_TOKEN_URI = "https://accounts.google.com/o/oauth2/token";
	
	static final String FILES_URI = "https://www.googleapis.com/drive/v2/files?maxResults=1000&q='%s'%%20in%%20parents";
	static final String FILE_URI  = "https://www.googleapis.com/drive/v2/files/%s";
	static final String ABOUT_URI = "https://www.googleapis.com/drive/v2/about";
		
	public static final String ROOT_PATH = "root";
	
	public final String clientId;
	public final String secretId;
	public final String redirectUri;
	public List<String> scopes = new ArrayList<String>();
	
	public String refreshToken;
	public String accessToken;
	public long expiresIn;
	
	public GoogleDriveApi(String clientId, String secretId, String redirectUri,
			String[] scopes) {
		this.clientId = clientId;
		this.secretId = secretId;
		this.redirectUri = redirectUri;
		for (String s : scopes)
			this.scopes.add(s);
	}

	public boolean isAuth() {
		return refreshToken != null && refreshToken.length() > 0;
	}
	
	public boolean isExpired() {
		return !isAuth() || (expiresIn <= System.currentTimeMillis() + 30000);
	}
	
	public void signOut() {
		this.refreshToken = null;
		this.accessToken = null;
		this.expiresIn = 0;
	}
	/**
	 * First step OAuth 2.0 authorization
	 * @return Url for browser
	 */
	public String getAuthorizationUrl() {
		try {
			StringBuilder str = new StringBuilder();
			for (String scope : scopes) {
				if (str.length() > 0)
					str.append(' ');
				str.append(scope);
			}
			return String.format(BASE_OAUTH_REDIRECT_URL, 
					URLEncoder.encode(clientId, "utf-8"),
					URLEncoder.encode(redirectUri, "utf-8"),
					URLEncoder.encode(str.toString(), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Second step OAuth 2.0 authorization.
	 * @param url - redirected url
	 * example: ?state=google-flow&code=<authorization code> 
	 */
	public boolean finishOAuthFlow(String url) {
		int i1 = url.indexOf('?');
		if (i1 < 0)
			return false;
		i1 = url.indexOf(QUERY_PARAM_CODE, i1);
		if (i1 < 0)
			return false;
		i1 += QUERY_PARAM_CODE.length();
		int i2 = url.indexOf("&", i1);
		String code;
		if (i2 < 0)
			code = url.substring(i1);
		else
			code = url.substring(i1, i2);
		return exchangeCode(code);
	}
	
	private boolean exchangeCode(String code) {
		try {
			String s = getPostRequest(OAUTH2_TOKEN_URI, 
					String.format("code=%s&grant_type=authorization_code&redirect_uri=%s&client_id=%s&client_secret=%s", 
					URLEncoder.encode(code, "utf-8"),
					URLEncoder.encode(redirectUri, "utf-8"),
					URLEncoder.encode(clientId, "utf-8"),
					URLEncoder.encode(secretId, "utf-8")));
			JSONObject obj = new JSONObject(s);
			if (obj.has("error")) {
				System.out.println("Error exchange code: " + obj.getString("error"));
				return false;
			}
			this.accessToken = obj.getString("access_token");
			this.expiresIn = System.currentTimeMillis() + obj.getInt("expires_in");
			this.refreshToken = obj.getString("refresh_token");
			return true;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean refreshToken() {
		if (!isAuth())
			return false;
		try {
			String s = getPostRequest(OAUTH2_TOKEN_URI, 
					String.format("refresh_token=%s&grant_type=refresh_token&client_id=%s&client_secret=%s", 
					URLEncoder.encode(refreshToken, "utf-8"),
					URLEncoder.encode(clientId, "utf-8"),
					URLEncoder.encode(secretId, "utf-8")));
			JSONObject obj = new JSONObject(s);
			if (obj.has("error")) {
				System.out.println("Error exchange code: " + obj.getString("error"));
				return false;
			}
			this.accessToken = obj.getString("access_token");
			this.expiresIn = System.currentTimeMillis() + obj.getInt("expires_in");
			return true;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void refreshTokenIfNeeded() {
		if (!isAuth())
			return;
		if (!isExpired())
			return;
		refreshToken();
	}
		
	public ArrayList<DriveFile> getFiles(String path) {
		if (!isAuth())
			return null;
		refreshTokenIfNeeded();
		try {
			String s = getGetRequest(String.format(FILES_URI, URLEncoder.encode(path, "utf-8")), streamContentListener);
			if (s == null)
				return null;
			JSONObject obj = new JSONObject(s);
			if (!obj.has("items"))
				return null;
			ArrayList<DriveFile> result = new ArrayList<DriveFile>();
			JSONArray items = obj.getJSONArray("items");
			for (int i = 0; i < items.length(); i ++) {
				JSONObject file = items.getJSONObject(i);
				result.add(new DriveFile(file));
			}
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public DriveFile getFile(String path) {
		if (!isAuth())
			return null;
		refreshTokenIfNeeded();
		try {
			String s = getGetRequest(String.format(FILE_URI, URLEncoder.encode(path, "utf-8")), streamContentListener);
			if (s == null)
				return null;
			return new DriveFile(new JSONObject(s));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public About getAbout() {
		if (!isAuth())
			return null;
		refreshTokenIfNeeded();
		try {
			String s = getGetRequest(ABOUT_URI, streamContentListener);
			if (s == null)
				return null;
			return new About(new JSONObject(s));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	Helpers.OnStreamContentListener streamContentListener = new Helpers.OnStreamContentListener() {
		
		@Override
		public void setRequests(URLConnection conn) {
			conn.addRequestProperty("Authorization", "Bearer " + accessToken);
		}
		
		@Override
		public boolean correctContentType(String type) {
			return true;
		}
	};
	
	public static String getGetRequest(String uri, Helpers.OnStreamContentListener listener) {
		InputStream in = Helpers.getStreamFromUri(uri, listener);
		StringBuilder str = new StringBuilder();
		int n;
		char[] buff = new char[512];
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		try {
			while((n = reader.read(buff)) >= 0) {
				str.append(buff, 0, n);
			}
			reader.close();
	  		return str.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getPostRequest(String uri, String body) throws IOException {
		URL url = new URL(uri);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		os.write(body.getBytes());
		
		int responseCode = conn.getResponseCode();
		
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		os.close();
		in.close();
		return response.toString();
	}
}
