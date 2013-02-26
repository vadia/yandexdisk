package org.vadel.common.yandexdisk;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.vadel.yandexdisk.authorization.Authorization;
import org.vadel.yandexdisk.authorization.BasicAuthorization;
import org.vadel.yandexdisk.authorization.OAuthAuthorization;

public class YandexDiskApi {
	
	public static boolean DEBUG = false;
	
	private static final String PARSE_TOKEN = "#access_token=";
	
	public static final String BASE_URI = "https://webdav.yandex.ru";
	
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
	
	static int BUFFER = 1024;
	
	public static final int ONE_MB = 1024*1024;
	public static final int TEN_MB = 10*ONE_MB;
	
	private long chunkSize = TEN_MB;
	
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
		int i1 = uri.indexOf(PARSE_TOKEN);
		if (i1 < 0)
			return;
		i1 += PARSE_TOKEN.length();
		int i2 = uri.indexOf("&", i1);
		if (i2 < 0)
			return;
		setToken(uri.substring(i1, i2));
	}

	public boolean isAuthorization() {
		return auth != null && auth.isValid();
	}
	
	public boolean createFolder(String path) {
		return executeWithoutResult(path, MKCOL);
	}
	
	public boolean delete(String path) {
		return executeWithoutResult(path, DELETE);
	}

	public boolean copy(String src, String dst) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Destination", dst);
		return executeWithoutResult(src, COPY);
	}

	public boolean move(String src, String dst) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Destination", dst);
		return executeWithoutResult(src, MOVE);
	}

	public boolean uploadFile(String path, InputStream dataStream, long fileLength) {
		InputStreamEntity entity = new InputStreamEntity(dataStream, fileLength);
		return executeWithoutResult(path, PUT, null, entity);
	}

	public boolean uploadFile(String path, String content) throws UnsupportedEncodingException {
		StringEntity entity = new StringEntity(content);
		return executeWithoutResult(path, PUT, null, entity);
	}

	public synchronized ArrayList<WebDavFile> getFiles(String path) {
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

	public long getChunkSize() {
		return chunkSize;
	}
	
	public void setChunkSize(long value) {
		if (value <= 0)
			return;
		chunkSize = value;
	}
	
	public synchronized long downloadFile(String path, FileOutputStream fos, long start,
			OnLoadProgressListener listener) {
		HashMap<String, String> params = null;
		if (start > 0) {
			params = new HashMap<String, String>();
			params.put("Range", "bytes=" + String.valueOf(start) + "-");
		}
		InputStream in = null;
		long lastProgress = -1;
		try {
			in = execute(path, GET, params);
			int n = 0;
			byte[] buffs = new byte[BUFFER];
			
			
        	if (listener != null) {
				while((n = in.read(buffs)) > 0) {
					fos.write(buffs, 0, n);
					start += n;
					
	        		long progress = start / chunkSize;
	        		if (lastProgress != progress) {
	        			try {
	        				listener.onProgress(start);
	        			} catch (InterruptedException e) {
	        				break;
	        			}
	        			lastProgress = progress;
	        		}
				}
        	} else {
				while((n = in.read(buffs)) > 0) {
					fos.write(buffs, 0, n);
					start += n;
				}
        	}
		} catch (IOException e) {
			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
		} finally {
			closeQuietly(in);
		}
		return start;
	}
	
	public String getFileString(String path) {
		return getFileString(path, 0);
	}

	public synchronized String getFileString(String path, long start) {
		HashMap<String, String> params = null;
		if (start > 0) {
			params = new HashMap<String, String>();
			params.put("Range", "bytes=" + String.valueOf(start) + "-");
		}
		try {
			return getStringFromStream(execute(path, GET, params));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected synchronized boolean executeWithoutResult(String path, String method) {
		return executeWithoutResult(path, method, null, null);
	}

	protected synchronized boolean executeWithoutResult(String path, String method, Map<String, String> params) {
		return executeWithoutResult(path, method, params, null);
	}

	protected synchronized boolean executeWithoutResult(String path, String method, Map<String, String> params,
			HttpEntity entity) {
		InputStream in = execute(path, method, params, entity);
		boolean result = in != null;
		closeQuietly(in);
		return result;
	}
	
	protected InputStream execute(String path, String method) {
		return execute(path, method, null, null);
	}

	protected InputStream execute(String path, String method, Map<String, String> params) {
		return execute(path, method, params, null);
	}

	protected InputStream execute(String path, String method, Map<String, String> params,
			HttpEntity entity) {
		if (!isAuthorization())
			return null;
		if (path == null || path.trim().length() == 0)
			path = "/";
		try {
			if (DEBUG) {
				System.out.println("***" + method + " " + path + "***");
			}
			WebDavRequest req = new WebDavRequest(BASE_URI + path, method);
			
			req.addHeader("Accept", "*/*");
			req.addHeader("Authorization", getAuthorization());
			if (params != null)
				for (String key : params.keySet()) 
					req.addHeader(key, params.get(key));
			if (entity != null) {
				req.setEntity(entity);
			}
			if (DEBUG) {
				System.out.println("Request Headers:");
				System.out.println(req.getRequestLine());
				for (Header h : req.getAllHeaders()) 
					System.out.println(h.getName() + ":" + h.getValue());
			}
			HttpClient client = new DefaultHttpClient();
			HttpResponse resp = client.execute(req);
			if (resp == null)
				return null;

			int code = resp.getStatusLine().getStatusCode();
			if (code != 201 && code != 200 && code != 206 && code != 207) {
				closeQuietly(resp.getEntity().getContent());
				return null;
//				throw new HttpResponseException(code, resp.getStatusLine().getReasonPhrase());
			}
			if (DEBUG) {
				System.out.println("Response Headers");
				System.out.println(resp.getStatusLine());
				for (Header h : resp.getAllHeaders()) 
					System.out.println(h.getName() + ":" + h.getValue());
				System.out.println();
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
		boolean notFirst = false;
		while ((line = reader.readLine()) != null) {
			if (notFirst)
				str.append('\n');
			str.append(line);
			notFirst = true;
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
	
	public static interface OnLoadProgressListener {
		
		void onProgress(long progress) throws InterruptedException;
		
	}
}
