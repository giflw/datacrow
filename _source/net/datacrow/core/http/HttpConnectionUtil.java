package net.datacrow.core.http;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Simplification for retrieving data from a specific address.
 */
public class HttpConnectionUtil {

    /**
     * Creates a new connection.
     * @param url
     * @return
     * @throws HttpConnectionException
     */
    public static HttpConnection getConnection(URL url) throws HttpConnectionException {
        return new HttpConnection(url);
    }
    
    /**
     * Retrieves the page content (UTF8).
     * @param url
     * @return
     * @throws HttpConnectionException
     */
    public static String retrievePage(String url) throws HttpConnectionException {
        return retrievePage(getURL(url), "UTF-8");
    }

    /**
     * Retrieves the page content using the supplied character set.
     * @param url
     * @param charset
     * @throws HttpConnectionException
     */
    public static String retrievePage(String url, String charset) throws HttpConnectionException {
        return retrievePage(getURL(url), charset);
    }

    /**
     * Retrieves the page content (UTF8).
     * @param url
     * @throws HttpConnectionException
     */
    public static String retrievePage(URL url) throws HttpConnectionException {
        return retrievePage(url, "UTF-8");
    }

    /**
     * Retrieves the page content using the supplied character set.
     * @param url
     * @param charset
     * @throws HttpConnectionException
     */
    public static String retrievePage(URL url, String charset) throws HttpConnectionException {
        HttpConnection connection = new HttpConnection(url);
        String page = connection.getString(charset);
        connection.close();
        return page;
    }

    /**
     * Retrieves the page content as a byte array.
     * @param url
     * @throws HttpConnectionException
     */
    public static byte[] retrieveBytes(String url) throws HttpConnectionException {
        return retrieveBytes(getURL(url));
    }

    /**
     * Retrieves the page content as a byte array.
     * @param url
     * @throws HttpConnectionException
     */
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
