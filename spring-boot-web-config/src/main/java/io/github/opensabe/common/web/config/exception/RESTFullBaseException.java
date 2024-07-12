package io.github.opensabe.common.web.config.exception;

import io.github.opensabe.common.web.config.base.ErrResponse;
import io.github.opensabe.common.web.config.base.ErrorUtil;
import lombok.Getter;

import java.io.Serializable;

/**
 * 17/8/17 下午7:31.
 *
 * @author zhaozhou
 */
public class RESTFullBaseException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 90291629754433147L;
    @Getter
    protected Integer code;

    RESTFullBaseException(String message) {
        super(message);
    }

    RESTFullBaseException(String message, int code) {
        super(message);
        this.code = code;
    }

    RESTFullBaseException(String msg, ErrResponse err) {
        super(ErrorUtil.appendError(msg, err));
        this.code = code;
    }
}
