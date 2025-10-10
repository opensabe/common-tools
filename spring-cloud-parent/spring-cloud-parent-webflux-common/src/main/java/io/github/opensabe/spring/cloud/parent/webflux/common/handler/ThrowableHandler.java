package io.github.opensabe.spring.cloud.parent.webflux.common.handler;

import io.github.opensabe.base.RespUtil;
import io.github.opensabe.base.vo.BaseRsp;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@Order
@RestControllerAdvice
public class ThrowableHandler {

    @ExceptionHandler(Throwable.class)
    public BaseRsp onThrowable (Throwable e, ServerHttpRequest request) {
        var path = request.getURI().getPath();
        log.error("{} error {}",path,e.getMessage(),e);
        return RespUtil.error(null, "Sorry,something went wrong. Please try again later.");
    }
}
