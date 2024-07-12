package io.github.opensabe.alive.client.exception;

public class AliveClientException extends Exception {

    private static final long serialVersionUID = 1L;

    public AliveClientException(String message) {
        super(message);
    }

    public AliveClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
