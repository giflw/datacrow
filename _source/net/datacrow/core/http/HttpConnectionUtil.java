package net.datacrow.core.http;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Simplification for retrieving data from a specific address.
 */
public class HttpConnectionUtil {

    public static HttpConnection getConnection(URL url) throws HttpConnectionException {
        return new HttpConnection(url);
    }
    
    public static String retrievePage(String url) throws HttpConnectionException {
        return retrievePage(getURL(url), "UTF-8");
    }

    public static String retrievePage(String url, String charset) throws HttpConnectionException {
        return retrievePage(getURL(url), charset);
    }

    public static String retrievePage(URL url) throws HttpConnectionException {
        return retrievePage(url, "UTF-8");
    }

    public static String retrievePage(URL url, String charset) throws HttpConnectionException {
        HttpConnection connection = new HttpConnection(url);
        String page = connection.getString(charset);
        connection.close();
        return page;
    }

    public static byte[] retrieveBytes(String url) throws HttpConnectionException {
        return retrieveBytes(getURL(url));
    }

    public static byte[] retrieveBytes(URL url) throws HttpConnectionException {
        HttpConnection connection = new HttpConnection(url);
        byte[] bytes = connection.getBytes();
        connection.close();
        return bytes;
    }
    
    private static URL getURL(String url) throws HttpConnectionException {
        try {
            return new URL(url);
        } catch (MalformedURLException mue) {
            throw new HttpConnectionException(mue);
        }
    }
}
