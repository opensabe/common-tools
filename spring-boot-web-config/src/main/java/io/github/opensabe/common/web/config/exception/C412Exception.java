package io.github.opensabe.common.web.config.exception;


import io.github.opensabe.common.web.config.base.ErrResponse;
import io.github.opensabe.common.web.config.base.ErrorUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class C412Exception extends RESTFull4xxBaseException implements Serializable {
    private static final long serialVersionUID = -2312393803704717855L;

    public C412Exception(String message) {
        super(message);
    }

    public C412Exception(String message, int code) {
        super(message, code);
    }

    public C412Exception(int code, String msg, ErrResponse err) {
        super(ErrorUtil.appendError(msg, err), code);
    }
}
