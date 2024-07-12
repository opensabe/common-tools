package io.github.opensabe.spring.cloud.parent.webflux.common.handler;

import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.base.vo.BaseRsp;
import io.github.opensabe.spring.cloud.parent.common.handler.BackendException;
import io.github.opensabe.spring.cloud.parent.common.handler.ErrorHandler;
import io.github.opensabe.spring.cloud.parent.common.handler.FrontendException;
import io.github.opensabe.spring.cloud.parent.common.handler.I18nMessageResolver;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;


/**
 * 全局异常处理
 * @author heng.ma
 */
@Log4j2
@RestControllerAdvice
public class GexceptionHandler extends ErrorHandler implements PriorityOrdered {

    private final I18nMessageResolver i18nMessageResolver;

    public GexceptionHandler(I18nMessageResolver i18nMessageResolver) {
        super();
        this.i18nMessageResolver = i18nMessageResolver;
    }

    @ExceptionHandler(FrontendException.class)
    public BaseRsp<?> onFrontendException(FrontendException e, ServerHttpRequest request) {
        log.info("{} error inner message {} return message {}", request.getURI().getPath(), e.getInnerMessage(), e.getMessage());
        return onFrontendException(e);
    }
    @ExceptionHandler(BackendException.class)
    public BaseRsp<?> onBackendException(BackendException e, ServerHttpRequest request) {
        log.info("{} error {}", request.getURI().getPath(), e.getMessage());
        return onBackendException(e);
    }
    @Override
    public BaseRsp<?> onFrontendException(FrontendException e) {
        return BaseRsp.builder()
                .data(e.getData())
                .message(i18nMessageResolver.resolveMessageTemplate(e.getMessage(), e.getArgs()))
                .innerMsg(e.getInnerMessage())
                .bizCode(e.getCode())
                .build();
    }

    @Override
    public BaseRsp<?> onBackendException(BackendException e) {
        return BaseRsp.builder()
                .data(e.getData())
                .message(e.getMessage())
                .innerMsg(e.getInnerMessage())
                .bizCode(e.getCode())
                .build();
    }

    @Override
    @ExceptionHandler(ConstraintViolationException.class)
    public BaseRsp<Void> onConstraintViolationException(ConstraintViolationException e) {
        return constraintViolationException(e);
    }

    @Override
    public BaseRsp<Void> onBindException(BindException e) {
        var message = message(e.getBindingResult());
        return BaseRsp.<Void>builder().bizCode(BizCodeEnum.INVALID.getVal()).message(message).build();
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public BaseRsp<Void> webExchangeBindException (WebExchangeBindException e) {
        var message = message(e.getBindingResult());
        return BaseRsp.<Void>builder().bizCode(BizCodeEnum.INVALID.getVal()).message(message).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseRsp<Void> validateException (MethodArgumentNotValidException e) {
        var message = message(e.getBindingResult());
        return BaseRsp.<Void>builder().bizCode(BizCodeEnum.INVALID.getVal()).message(message).build();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
