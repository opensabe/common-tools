package io.github.opensabe.common.web.config.exception;

/**
 * 17/8/17 下午7:33.
 *
 * @author zhaozhou
 */
public class RESTFull2xxBaseException extends RESTFullBaseException {
    public RESTFull2xxBaseException(String message) {
        super(message);
    }

    public RESTFull2xxBaseException(String message, int code) {
        super(message, code);
    }
}
