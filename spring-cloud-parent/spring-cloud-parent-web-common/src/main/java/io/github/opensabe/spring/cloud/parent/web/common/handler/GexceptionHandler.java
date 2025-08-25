/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.spring.cloud.parent.web.common.handler;

import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.base.vo.BaseRsp;
import io.github.opensabe.spring.cloud.parent.common.handler.BackendException;
import io.github.opensabe.spring.cloud.parent.common.handler.ErrorHandler;
import io.github.opensabe.spring.cloud.parent.common.handler.FrontendException;
import io.github.opensabe.spring.cloud.parent.common.handler.I18nMessageResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;


/**
 * 全局异常处理
 *
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
    public BaseRsp<?> onFrontendException(FrontendException e, HttpServletRequest request) {
        log.info("{} error inner message {} return message {}", request.getRequestURI(), e.getInnerMessage(), e.getMessage());
        return onFrontendException(e);
    }

    @Override
    public BaseRsp<?> onFrontendException(FrontendException e) {
        return BaseRsp.builder()
                .bizCode(e.getCode())
                .message(i18nMessageResolver.resolveMessageTemplate(e.getMessage(), e.getArgs()))
//                .innerMsg(e.getInnerMessage())
                .data(e.getData())
                .build();
    }

    @Override
    public BaseRsp<?> onBackendException(BackendException e) {
        return BaseRsp.builder()
                .bizCode(e.getCode())
                .message(e.getMessage())
                .innerMsg(e.getInnerMessage())
                .data(e.getData())
                .build();
    }

    @ExceptionHandler(BackendException.class)
    public BaseRsp<?> onBackendException(BackendException e, HttpServletRequest request) {
        log.info("{} error {}", request.getRequestURI(), e.getMessage());
        return onBackendException(e);
    }

    @Override
    @ExceptionHandler(ConstraintViolationException.class)
    public BaseRsp<?> onConstraintViolationException(ConstraintViolationException e) {
        return constraintViolationException(e);
    }

    @ExceptionHandler(BindException.class)
    public BaseRsp<?> onBindException(BindException e) {
        var message = message(e.getBindingResult());
        return BaseRsp.builder().bizCode(BizCodeEnum.INVALID.getVal()).message(message).build();
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public BaseRsp<?> webExchangeBindException(WebExchangeBindException e) {
        var message = message(e.getBindingResult());
        return BaseRsp.builder().bizCode(BizCodeEnum.INVALID.getVal()).message(message).build();
    }

    /**
     * 参数校验
     *
     * @param e
     * @return BaseRsp
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseRsp<?> validateException(MethodArgumentNotValidException e) {
        var message = message(e.getBindingResult());
        return BaseRsp.builder().bizCode(BizCodeEnum.INVALID.getVal()).message(message).build();
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public BaseRsp<?> servletRequestBindingException(ServletRequestBindingException e) {
        return BaseRsp.builder().bizCode(BizCodeEnum.INVALID.getVal()).message(e.getMessage()).build();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
