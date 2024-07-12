package io.github.opensabe.common.web.config.exception;


import io.github.opensabe.common.web.config.base.ErrResponse;
import io.github.opensabe.common.web.config.base.ErrorUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

@ResponseStatus(HttpStatus.OK)
public class C200Exception extends RESTFull2xxBaseException implements Serializable {
    private static final long serialVersionUID = -2312393803704717855L;

    public C200Exception(String message) {
        super(message);
    }

    public C200Exception(int code, String message) {
        super(message, code);
    }

    public C200Exception(int code, String msg, ErrResponse err) {
        super(ErrorUtil.appendError(msg, err), code);
    }
}
