package org.vadel.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Created by vadimbabin on 24.04.14.
 */
public class Helpers {

    /**
     * The masks used to validate and parse the input to this Atom date. These
     * are a lot more forgiving than what the Atom spec allows. The forms that
     * are invalid according to the spec are indicated.
     */
    private static final String[] masksAtom = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSz",
            "yyyy-MM-dd't'HH:mm:ss.SSSz", // invalid
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd't'HH:mm:ss.SSS'z'", // invalid
            "yyyy-MM-dd'T'HH:mm:ssz", "yyyy-MM-dd't'HH:mm:ssz", // invalid
            "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd't'HH:mm:ss'z'", // invalid
            "yyyy-MM-dd'T'HH:mmz", // invalid
            "yyyy-MM-dd't'HH:mmz", // invalid
            "yyyy-MM-dd'T'HH:mm'Z'", // invalid
            "yyyy-MM-dd't'HH:mm'z'", // invalid
            "yyyy-MM-dd", "yyyy-MM", "yyyy" };

    /**
     * Parse the serialized string form into a java.util.Date
     *
     * @param date
     *            The serialized string form of the date
     * @return The created java.util.Date
     */
    public static long parseAtomDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        for (String s : masksAtom) {
            try {
                sdf.applyPattern(s);
                return sdf.parse(date).getTime();
            } catch (Exception e) {
            }
        }
        return 0;
    }

    public static int TIMEOUT_CONNECTION = 20000;
    public static final String CHROME_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36";

    public static InputStream getStreamFromUri(String uri, OnStreamContentListener listener) {
        try {
            URL Url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
            if (conn == null)
                return null;
            HttpURLConnection.setFollowRedirects(true);
            conn.setReadTimeout(TIMEOUT_CONNECTION);
            conn.setConnectTimeout(TIMEOUT_CONNECTION);
            conn.setRequestProperty("User-Agent", CHROME_USER_AGENT);
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            if (listener != null) {
                listener.setRequests(conn);
            }
            if (listener != null) {
                if (!listener.correctContentType(conn.getContentType()))
                    return null;
            }

//			if (conn instanceof HttpsURLConnection) {
//				HttpsURLConnection https = (HttpsURLConnection) conn;
//				System.out.println("Response(https): " + https.getResponseCode());
//			} else if (conn instanceof HttpURLConnection) {
//	  			HttpURLConnection http = (HttpURLConnection) conn;
//	  			System.out.println("Response(http): " + http.getResponseCode());
//			}
            InputStream in = getInputEncoding(conn);
//	  		for (String key : conn.getHeaderFields().keySet())
//	  			System.out.println(key + " : " + conn.getHeaderField(key));
            return in;
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

    public interface OnStreamContentListener {
        void setRequests(URLConnection conn);

        boolean correctContentType(String type);
    }

    /**
     * получение имени страницы из ссылки (http://manga.com/manga1/chapter1.htm -> chapter1.htm)
     * @param link - исходная ссылка, (http://manga.com/manga1/chapter1.htm)
     * @return название страницы, (chapter1.htm)
     */
    public static String getPageName(String link, boolean cutQueries) {
        String s = link.trim();
        if (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        int i2 = -1;
        if (cutQueries)
            i2 = link.indexOf('?');
        if (i2 < 0)
            i2 = s.length();

        int i1 = s.lastIndexOf('/', i2) + 1;
//		int i2 = -1;
//		if (cutQueries)
//			i2 = link.indexOf('?');
//		if (i2 < 0)
//			i2 = s.length();
        if (i1 < 0)
            i1 = 0;
        return s.substring(i1, i2);
    }

    public static String getPageName(String link) {
        return getPageName(link, false);
    }
}
