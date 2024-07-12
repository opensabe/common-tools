package io.github.opensabe.common.web.config.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class C400Exception extends RESTFull4xxBaseException implements Serializable {
    private static final long serialVersionUID = -2312393803704717855L;

    public C400Exception(String message) {
        super(message);
    }

    public C400Exception(String message, int code) {
        super(message, code);
    }
}
