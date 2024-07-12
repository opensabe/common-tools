package io.github.opensabe.alive.client.exception;


public class AliveClientTimeoutException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AliveClientTimeoutException(String message) {
        super(message);
    }

    public AliveClientTimeoutException(String message, Throwable t) {
        super(message, t);
    }

    public AliveClientTimeoutException() {
        super();
    }

}
