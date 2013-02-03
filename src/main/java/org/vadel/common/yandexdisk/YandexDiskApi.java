package org.vadel.common.yandexdisk;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.vadel.yandexdisk.authorization.Authorization;
import org.vadel.yandexdisk.authorization.BasicAuthorization;
import org.vadel.yandexdisk.authorization.OAuthAuthorization;

public class YandexDiskApi {
	
	protected static final String BASE_URI = "https://webdav.yandex.ru";
	protected static final String BASE_OAUTH_AUTHORIZE_URL = 
		"https://oauth.yandex.ru/authorize?response_type=token&client_id=";
	
	static final String PUT       = "PUT"; 
	static final String GET       = "GET";
	static final String MKCOL     = "MKCOL";
	static final String COPY      = "COPY";
	static final String MOVE      = "MOVE";
	static final String DELETE    = "DELETE";
	static final String PROPFIND  = "PROPFIND";
	static final String PROPPATCH = "PROPPATCH";
	
	final HttpClient client = new DefaultHttpClient();
	
	protected final String clientId;

	protected Authorization auth;
	
	public YandexDiskApi(String clientId) {
		this.clientId = clientId;
	}
	
	public String getLogin() {
		if (auth instanceof BasicAuthorization)
			return ((BasicAuthorization) auth).login;
		else
			return null;
	}

	public String getPassword() {
		if (auth instanceof BasicAuthorization)
			return ((BasicAuthorization) auth).pass;
		else
			return null;
	}
	
	public void setCredentials(String login, String pass) {
		auth = new BasicAuthorization(login, pass);
	}
	
	public String getOAthRequestUrl() {
		return BASE_OAUTH_AUTHORIZE_URL + clientId;
	}
	
	public String getToken() {
		if (auth instanceof OAuthAuthorization)
			return ((OAuthAuthorization) auth).token;
		else
			return null;
	}
	
	public void setToken(String value) {
		auth = new OAuthAuthorization(value);
	}
	
	/**
	 * Response example: http://aaaaa.aaaaa.com/callback#access_token=00a11b22c3333c44a7e7d6db623bd5e0&token_type=bearer&state=
	 * @param value - Response uri
	 */
	public void setTokenFromCallBackURI(String uri) {
		int i1 = uri.indexOf('#');
		if (i1 < 0)
			return;
		i1 ++;
		int i2 = uri.indexOf("&", i1);
		if (i2 < 0)
			return;
		setToken(uri.substring(i1, i2));
	}

	public boolean isAuthorization() {
		return auth != null && auth.isValid();
	}
	
//	public long getDiskLimit() {
//		return -1;
//	}
	
	public void createFolder(String path) throws YandexDiskException {
		executeWithoutResult(path, MKCOL);
	}
	
	public void delete(String path) throws YandexDiskException {
		executeWithoutResult(path, DELETE);
	}

	public void copy(String src, String dst) throws YandexDiskException {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Destination", dst);
		executeWithoutResult(src, COPY);
	}

	public void move(String src, String dst) throws YandexDiskException {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Destination", dst);
		executeWithoutResult(src, MOVE);
	}

	public void uploadFile(String path, InputStream dataStream, long fileLength) throws YandexDiskException {
		InputStreamEntity entity = new InputStreamEntity(dataStream, fileLength);
		executeWithoutResult(path, PUT, null, entity);
	}

	public void uploadFile(String path, String content) throws YandexDiskException, UnsupportedEncodingException {
		StringEntity entity = new StringEntity(content);
		executeWithoutResult(path, PUT, null, entity);
	}

	public ArrayList<WebDavFile> getFiles(String path) throws YandexDiskException {
		InputStream in = null;
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Depth", "1");
		try {
			in = execute(path, PROPFIND, params);
			return XmlResponseReader.getFilesFromStream(in);
		} finally {
			closeQuietly(in);
		}
	}

	public InputStream getFileStream(String path) throws YandexDiskException {
		return getFileStream(path, 0);
	}

	public InputStream getFileStream(String path, long start) throws YandexDiskException {
		HashMap<String, String> params = null;
		if (start > 0) {
			params = new HashMap<String, String>();
			params.put("Range", String.valueOf(start));
		}
		return execute(path, GET, params);
	}

	public void executeWithoutResult(String path, String method) throws YandexDiskException {
		executeWithoutResult(path, method, null, null);
	}

	public void executeWithoutResult(String path, String method, Map<String, String> params) throws YandexDiskException {
		executeWithoutResult(path, method, params, null);
	}

	public void executeWithoutResult(String path, String method, Map<String, String> params,
			HttpEntity entity) throws YandexDiskException {
		InputStream in = null;
		try {
			in = execute(path, method, params, entity);
		} finally {
			closeQuietly(in);
		}
	}
	
	public InputStream execute(String path, String method) throws YandexDiskException {
		return execute(path, method, null, null);
	}

	public InputStream execute(String path, String method, Map<String, String> params) throws YandexDiskException {
		return execute(path, method, params, null);
	}

	public InputStream execute(String path, String method, Map<String, String> params,
			HttpEntity entity) throws YandexDiskException {
		if (!isAuthorization())
			return null;
		if (path == null || path.trim().length() == 0)
			path = "/";
		try {
			WebDavRequest req = new WebDavRequest(BASE_URI + path, method);
			
			req.addHeader("Accept", "*/*");
			req.addHeader("Authorization", getAuthorization());
			if (params != null)
				for (String key : params.keySet()) 
					req.addHeader(key, params.get(key));
			if (entity != null) {
				req.setEntity(entity);
			}
			
			HttpResponse resp = client.execute(req);
			if (resp == null)
				return null;

			int code = resp.getStatusLine().getStatusCode();
			if (code != 201 && code != 200 && code != 207) {
				throw new HttpResponseException(code, resp.getStatusLine().getReasonPhrase());
			}
			return resp.getEntity().getContent();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected String getAuthorization() {
		if (auth != null && auth.isValid())
			return auth.getAuthorizationHeader();
		else
			return null;
	}
	
	public static String getStringFromStream(InputStream in) throws IOException {
		InputStreamReader isr = new InputStreamReader(in);
		BufferedReader reader = new BufferedReader(isr);
		StringBuilder str = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
//			System.out.println(line);
			str.append(line);
			str.append('\n');
		}
		return str.toString();
	}
	
	public static void closeQuietly(Closeable in) {
		if (in != null)
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
//	static class RequstCommand {
//		public String method;
//		public int okCode;
//		
//		public RequstCommand(String method, int code) {
//			this.method = method;
//			this.okCode = code;
//		}
//	}
}
