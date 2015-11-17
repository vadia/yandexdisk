package org.vadel.dropbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class DropboxApi {

	public enum RootEndPoint { sandbox, dropbox };

	static final String AUTHORIZE = "https://www.dropbox.com/1/oauth2/authorize?response_type=token&client_id=%s&redirect_uri=%s";

	static final String ACCOUNT   = "https://api.dropbox.com/1/account/info/";
	static final String FILES     = "https://api-content.dropbox.com/1/files/";
	static final String METADATA  = "https://api.dropbox.com/1/metadata/";
	static final String MEDIA     = "https://api.dropbox.com/1/media/";

	final String clientId;
	final String redirectUri;

	public String accessToken;
	public String uid;

	RootEndPoint root = RootEndPoint.dropbox;

	public DropboxApi(String clientId, String redirectUri) {
		this.clientId = clientId;
		this.redirectUri = redirectUri;
	}
	
	public String getAuthoriztionUrl() {
		return String.format(AUTHORIZE, clientId, redirectUri);
	}

	public void finishAuthoriztion(String uri) {
		if (uri == null)
			return;
			
		int i = uri.indexOf('?');
		if (i < 0) {
			i = uri.indexOf('#');
			if (i < 0) 
				return;
		}
			
		uri = uri.substring(i + 1);
		String[] values = uri.split("&");
		for (String value : values) {
			String[] param = value.split("=");
			if (param.length != 2)
				continue;
			if (param[0].equals("access_token"))
				this.accessToken = param[1];
			else if (param[0].equals("uid"))
				this.uid = param[1];
		}
	}

	public void setAccessToken(String accessToken, String uid) {
		this.accessToken = accessToken;
		this.uid = uid;
	}
	
	public void signOff() {
		this.accessToken = null;
		this.uid = null;
	}

	public boolean isAuthenticated() {
		return this.accessToken != null;
	}

	public void setEndPoint(RootEndPoint value) {
		this.root = value;
	}

	public InputStream getFileStream(String path) {
		if (!isAuthenticated())
			return null;
		String uri = FILES + root + "/" + encodePaths(path) + "?access_token=" + accessToken;
		return getStreamFromUri(uri);
	}

	public String getFileString(String path) {
		if (!isAuthenticated())
			return null;
		String uri = FILES + root + "/" + encodePaths(path) + "?access_token=" + accessToken;
		return getStringFromUrl(uri);
	}
	
	public AccountInfo accountInfo() {
		if (!isAuthenticated())
			return null;
		return accountInfo(accessToken);
	}
	
	public DropFile metadata(String path) {
		if (!isAuthenticated())
			return null;
		return metadata(root, accessToken, path);
	}

	public MediaUrl media(String path) {
		if (!isAuthenticated())
			return null;
		return media(root, accessToken, path);
	}
	
	public static AccountInfo accountInfo(String accessToken) {
		String uri = ACCOUNT + "?access_token=" + accessToken;
		String s = getStringFromUrl(uri);
		if (s == null)
			return null;
		return AccountInfo.fromJSON(s);
	}

	public static DropFile metadata(RootEndPoint endPoint, String accessToken, String path) {
		if (!path.startsWith("/"))
			path = '/' + path;
		
		String uri = METADATA + endPoint + encodePaths(path) + "?include_deleted=false&list=true&access_token=" + accessToken;
		String s = getStringFromUrl(uri);
		if (s == null)
			return null;
		return DropFile.fromJSON(s);
	}
	
	public static MediaUrl media(RootEndPoint endPoint, String accessToken, String path) {
		String uri = MEDIA + endPoint + "/" + encodePaths(path) + "?access_token=" + accessToken;
		String s = getStringFromUrl(uri);
		if (s == null)
			return null;
		return MediaUrl.fromMediaUrl(s);
	}
	
	// public static int TIMEOUT_CONNECTION = 20000;
    
	public static String getStringFromUrl(String uri) {
		InputStream in = getStreamFromUri(uri);
		if (in == null)
			return null;
		try {
			StringBuilder str = new StringBuilder();
			int n;
  			char[] buff = new char[512];
  			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
  			while((n = reader.read(buff)) >= 0) {
  				str.append(buff, 0, n);
  			}
  			reader.close();
	  		return str.toString();
	  	} catch (MalformedURLException e) {
	  		System.out.println("Error! Can't download uri: " + uri);
	  		e.printStackTrace();
	  	} catch (IOException e) {
	  		System.out.println("Error! Can't download uri: " + uri);
	  		e.printStackTrace();
	  	}
  		return null;
	}
	
	public static InputStream getStreamFromUri(String uri) {
		try {
	  		URL Url = new URL(uri);
	  		URLConnection conn = Url.openConnection();
	  		if (conn == null)
	  			return null;
	  		HttpURLConnection.setFollowRedirects(true);	
	  		// conn.setReadTimeout(TIMEOUT_CONNECTION);
	  		// conn.setConnectTimeout(TIMEOUT_CONNECTION);
//	  		conn.setRequestProperty("User-Agent", GlobalLinksUtils.CHROME_USER_AGENT);
	  		conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
	  		// if (listener != null) {
	  		// 	listener.setRequests(conn);
	  		// }
	  		// if (listener != null && !listener.correctContentType(conn.getContentType())) 
  			// 	return null;
	  		
//			if (conn instanceof HttpsURLConnection) {
//				HttpsURLConnection https = (HttpsURLConnection) conn;
//				System.out.println("Response(https): " + https.getResponseCode());
//			} else if (conn instanceof HttpURLConnection) {
//	  			HttpURLConnection http = (HttpURLConnection) conn;
//	  			System.out.println("Response(http): " + http.getResponseCode());
//			}
  			return getInputEncoding(conn);
	  	} catch (MalformedURLException e) {
	  		e.printStackTrace();
	  		return null;
	  	} catch (IOException e) {
	  		e.printStackTrace();
	  		return null;
	  	}
	}  

	public static InputStream getInputEncoding(URLConnection connection) throws IOException {
		InputStream in;
		String encoding = connection.getContentEncoding();
		if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
			in = new GZIPInputStream(connection.getInputStream());
		} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
			in = new InflaterInputStream(connection.getInputStream(), new Inflater(true));
		} else {
			in = connection.getInputStream();
		}
		return in;
	}
	
	public static String encodePaths(String path) {
		StringBuilder str = new StringBuilder();
//		if (path.startsWith("/"))
//			str.append('/');
		try {
			for (String s : path.split("/")) {
				if (s.length() == 0)
					continue;
//				if (str.length() == 0 || str.charAt(str.length() - 1) != '/')
//					str.append('/');
				str.append('/').append(URLEncoder.encode(s, "utf-8").replace("+", "%20"));
			}
			if (path.endsWith("/"))
				str.append('/');
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str.toString();
	}
}
