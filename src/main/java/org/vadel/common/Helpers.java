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

    private final static char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
            .toCharArray();

    private static int[] toInt = new int[128];

    static {
        for (int i = 0; i < ALPHABET.length; i++) {
            toInt[ALPHABET[i]] = i;
        }
    }

    /**
     * Translates the specified byte array into Base64 string.
     * 
     * @param buf
     *            the byte array (not null)
     * @return the translated Base64 string (not null)
     */
    public static String encode(byte[] buf) {
        int size = buf.length;
        char[] ar = new char[((size + 2) / 3) * 4];
        int a = 0;
        int i = 0;
        while (i < size) {
            byte b0 = buf[i++];
            byte b1 = (i < size) ? buf[i++] : 0;
            byte b2 = (i < size) ? buf[i++] : 0;

            int mask = 0x3F;
            ar[a++] = ALPHABET[(b0 >> 2) & mask];
            ar[a++] = ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
            ar[a++] = ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
            ar[a++] = ALPHABET[b2 & mask];
        }
        switch (size % 3) {
        case 1:
            ar[--a] = '=';
        case 2:
            ar[--a] = '=';
        }
        return new String(ar);
    }

    /**
     * Translates the specified Base64 string into a byte array.
     * 
     * @param s
     *            the Base64 string (not null)
     * @return the byte array (not null)
     */
    public static byte[] decode(String s) {
        int delta = s.endsWith("==") ? 2 : s.endsWith("=") ? 1 : 0;
        byte[] buffer = new byte[s.length() * 3 / 4 - delta];
        int mask = 0xFF;
        int index = 0;
        for (int i = 0; i < s.length(); i += 4) {
            int c0 = toInt[s.charAt(i)];
            int c1 = toInt[s.charAt(i + 1)];
            buffer[index++] = (byte) (((c0 << 2) | (c1 >> 4)) & mask);
            if (index >= buffer.length) {
                return buffer;
            }
            int c2 = toInt[s.charAt(i + 2)];
            buffer[index++] = (byte) (((c1 << 4) | (c2 >> 2)) & mask);
            if (index >= buffer.length) {
                return buffer;
            }
            int c3 = toInt[s.charAt(i + 3)];
            buffer[index++] = (byte) (((c2 << 6) | c3) & mask);
        }
        return buffer;
    }
}
