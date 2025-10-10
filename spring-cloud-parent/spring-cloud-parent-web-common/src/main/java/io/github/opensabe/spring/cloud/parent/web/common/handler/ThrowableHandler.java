package io.github.opensabe.spring.cloud.parent.web.common.handler;

import io.github.opensabe.base.RespUtil;
import io.github.opensabe.base.vo.BaseRsp;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Log4j2
@Order
@RestControllerAdvice
public class ThrowableHandler {
    @ExceptionHandler(Throwable.class)
    public BaseRsp onThrowable (Throwable e, HttpServletRequest request) {
        var path = request.getRequestURI();
        log.error("{} error {}",path, e.getMessage(), e);
        return RespUtil.error(null,"Sorry, something went wrong. Please try again later.");
    }
}
