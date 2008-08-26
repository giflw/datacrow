package net.datacrow.core.http;

public class HttpConnectionException extends Exception {

    private static final long serialVersionUID = 8808884146405903523L;

    public HttpConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpConnectionException(Throwable cause) {
        super(cause);
    }
}
