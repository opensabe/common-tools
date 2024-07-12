package io.github.opensabe.spring.cloud.parent.common.handler;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IException extends RuntimeException{
    private final Integer code;
    private final Object data;
    private final String innerMessage;

    public IException(Integer code, String message, String innerMessage, Object data) {
        super(message);
        this.code = code;
        this.data = data;
        this.innerMessage = innerMessage;
    }
    public IException(Integer code, String message, Object data) {
        this(code, message, null, data);
    }
    public IException(Integer code, String message, String innerMessage) {
        this(code, message, innerMessage, null);
    }
    public IException(Integer code, String message) {
        this(code, message, null, null);
    }
    public IException(ErrorMessage message, String innerMessage, Object data) {
        this(message.code(), message.message(), innerMessage, data);
    }
    public IException(ErrorMessage message, String innerMessage) {
        this(message, innerMessage, null);
    }
    public IException(ErrorMessage message, Object data) {
        this(message, null, data);
    }
    public IException(ErrorMessage message) {
        this(message, null, null);
    }
}
