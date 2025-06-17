package io.github.opensabe.spring.cloud.parent.common.handler;

import io.github.opensabe.base.vo.BaseRsp;

public class BackendException extends IException {

    public static <T> T resolveBaseResponse (BaseRsp<T> rsp) {
        return rsp.resolveData(BackendException::new);
    }

    public BackendException(Integer code, String message, String innerMessage, Object data) {
        super(code, message, innerMessage, data);
    }

    public BackendException(Integer code, String message, Object data) {
        super(code, message, data);
    }

    public BackendException(Integer code, String message, String innerMessage) {
        super(code, message, innerMessage);
    }

    public BackendException(Integer code, String message) {
        super(code, message);
    }

    public BackendException(ErrorMessage message, String innerMessage, Object data) {
        super(message, innerMessage, data);
    }

    public BackendException(ErrorMessage message, String innerMessage) {
        super(message, innerMessage);
    }

    public BackendException(ErrorMessage message, Object data) {
        super(message, data);
    }

    public BackendException(ErrorMessage message) {
        super(message);
    }
}
