package io.github.opensabe.common.web.config.base;

import lombok.Data;

/**
 * 17/9/28 下午12:25.
 *
 * @author zhaozhou
 */
@Data
public class ErrResponse<T> {
    private String message;
    private T data;

    public ErrResponse() {
    }

    public ErrResponse(T data) {
        this.data = data;
    }
}
