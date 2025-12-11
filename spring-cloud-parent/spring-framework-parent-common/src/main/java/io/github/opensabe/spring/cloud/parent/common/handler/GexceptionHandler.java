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
package io.github.opensabe.spring.cloud.parent.common.handler;

import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.base.vo.BaseRsp;
import io.github.opensabe.spring.cloud.parent.common.web.Path;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Objects;


/**
 * 全局异常处理
 *
 * @author heng.ma
 */
@Log4j2
@RestControllerAdvice
public class GexceptionHandler implements PriorityOrdered {

    private final I18nMessageResolver i18nMessageResolver;

    public GexceptionHandler(I18nMessageResolver i18nMessageResolver) {
        super();
        this.i18nMessageResolver = i18nMessageResolver;
    }

    @ExceptionHandler(FrontendException.class)
    public BaseRsp<?> onFrontendException(FrontendException e, @Path String path) {
        log.info("{} error inner message {} return message {}", path, e.getInnerMessage(), e.getMessage());
        return BaseRsp.builder()
                .bizCode(e.getCode())
                .message(i18nMessageResolver.resolveMessageTemplate(e.getMessage(), e.getArgs()))
//                .innerMsg(e.getInnerMessage())
                .data(e.getData())
                .build();
    }



    @ExceptionHandler(BackendException.class)
    public BaseRsp<?> onBackendException(BackendException e, @Path String path) {
        log.info("{} error {}", path, e.getMessage());
        return BaseRsp.builder()
                .bizCode(e.getCode())
                .message(e.getMessage())
                .innerMsg(e.getInnerMessage())
                .data(e.getData())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public BaseRsp<Void> onConstraintViolationException(ConstraintViolationException e) {
        return BaseRsp.<Void>builder().bizCode(BizCodeEnum.INVALID.getVal()).message(e.getMessage()).build();
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public BaseRsp<Void> webExchangeBindException(WebExchangeBindException e) {
        return resolveBindingResult(e, e.getMessage());
    }



    @ExceptionHandler(BindException.class)
    public BaseRsp<Void> onBindException(BindException e) {
        return resolveBindingResult(e, e.getMessage());
    }

//
//    @ExceptionHandler(MissingRequestValueException.class)
//    public BaseRsp<Void> onMissingRequestValueException(MissingRequestValueException e, @Path String path) {
//        log.info("{}: {}", path, e.getMessage());
//        return RespUtil.invalid(null);
//    }

    private BaseRsp<Void> resolveBindingResult (BindingResult bindingResult, String fallbackMessage) {
        FieldError fieldError = bindingResult.getFieldError();
        String message = fallbackMessage;
        if (Objects.nonNull(fieldError)) {
            message = resolveFieldError(fieldError);
        }else if (Objects.nonNull(bindingResult.getGlobalError())){
            message = resolveGlobalError(bindingResult.getGlobalError());
        }
        return BaseRsp.<Void>builder().bizCode(BizCodeEnum.INVALID.getVal()).message(message).build();
    }

    private  String resolveFieldError (FieldError fieldError) {
        String objectName = fieldError.getObjectName();
        String nestedPath = fieldError.getField();
        String defaultMessage = fieldError.getDefaultMessage();
        return objectName+"."+nestedPath +" "+defaultMessage;
    }
    private  String resolveGlobalError (ObjectError objectError) {
        String objectName = objectError.getObjectName();
        String nestedPath = objectError.getCode();
        String defaultMessage = objectError.getDefaultMessage();
        return objectName+"."+nestedPath +" "+defaultMessage;
    }



    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
