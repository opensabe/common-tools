package io.github.opensabe.alive.client.exception;

public class AliveClientRuntimeException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 5550471458078575358L;

    public AliveClientRuntimeException() {
        super();
    }

    public AliveClientRuntimeException(String message) {
        super(message);
    }

    public AliveClientRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
