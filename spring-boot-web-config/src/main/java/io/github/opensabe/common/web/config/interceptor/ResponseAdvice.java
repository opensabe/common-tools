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
package io.github.opensabe.common.web.config.interceptor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.base.vo.BaseRsp;
import io.github.opensabe.common.web.config.base.ErrorUtil;
import io.github.opensabe.common.web.config.exception.RESTFullBaseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import lombok.extern.log4j.Log4j2;


@RestControllerAdvice
@Log4j2
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    private static final Map HASH_MAP = new HashMap();
    @Value("${debug:false}")
    private boolean debugOpen;

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        //所有的endpoint和swagger全都不拦截
        String name = methodParameter.getMethod().toString().toLowerCase();
        boolean swagger = name.contains("swagger");
        boolean apidocs = name.contains("openapi");
        boolean actuator = name.contains("actuate.endpoint");
        return !actuator
                && !swagger && !apidocs;
    }

    @Override
    public BaseRsp beforeBodyWrite(Object obj, MethodParameter methodParameter, MediaType mediaType,
                                   Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest,
                                   ServerHttpResponse serverHttpResponse) {
        if (obj instanceof BaseRsp) {
            return (BaseRsp) obj;
        }
        return BaseRsp.builder().bizCode(BizCodeEnum.SUCCESS.getVal()).message(BizCodeEnum.SUCCESS.getDefaultMsg()).data(obj).build();
    }

    /**
     * Biz exception handle
     *
     * @param response http response
     * @return 标准返回模型
     */
    @ExceptionHandler(Exception.class)
    public BaseRsp handleUnexpectedServerError(Exception exception, HttpServletRequest request, HttpServletResponse response) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        BaseRsp.BaseRspBuilder<Object> builder = BaseRsp.builder().data(HASH_MAP);
        if (exception instanceof RESTFullBaseException) {
            RESTFullBaseException bce = (RESTFullBaseException) exception;
            ErrorUtil.handleBizExceptionMsg(builder, bce);
            ErrorUtil.handleBizCode(requestURI, method, builder, bce);
            response.setStatus(getHttpStatus((RESTFullBaseException) exception));
            if (debugOpen) {
                builder.innerMsg(ErrorUtil.getDebug(exception));
            }
            return builder.build();
        }
        log.error("URI:[{}], method:[{}], 500Exception: {}, {}", requestURI, method, exception.getClass(), exception.getMessage(), exception);
        response.setStatus(500);
        if (debugOpen) {
            builder.innerMsg(ErrorUtil.getDebug(exception));
        }
        return builder.bizCode(BizCodeEnum.ERROR.getVal()).message(BizCodeEnum.ERROR.getDefaultMsg()).build();
    }

    /**
     * Get http code from CxxxException
     *
     * @param e RESTFullBaseException
     * @return http code
     */
    private int getHttpStatus(RESTFullBaseException e) {
        return e.getClass().getAnnotation(ResponseStatus.class).value().value();
    }

    /**
     * FORBIDDEN
     *
     * @return ResponseModel
     */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public BaseRsp httpRequestMethodNotSupportedExceptionHandler() {
        return BaseRsp.builder().bizCode(BizCodeEnum.FORBIDDEN.getVal()).message(BizCodeEnum.FORBIDDEN.getDefaultMsg()).build();
    }

    /**
     * Invalid
     *
     * @param exception MissingServletRequestParameterException ServletRequestBindingException MethodArgumentNotValidException
     * @return ResponseModel
     */
    @ExceptionHandler(value = {
            MissingServletRequestParameterException.class,
            MissingPathVariableException.class,
            ServletRequestBindingException.class,
            UnexpectedTypeException.class,
            UnsatisfiedServletRequestParameterException.class,
            HttpMediaTypeException.class,
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseRsp invalidException(HttpServletRequest request, Exception exception) {
        log.info("ResponseAdvice-invalidException: {}", exception.getMessage());
        return BaseRsp.builder()
                .bizCode(BizCodeEnum.INVALID.getVal())
                .innerMsg(debugOpen ? exception.toString() : null)
                .message(BizCodeEnum.INVALID.getDefaultMsg())
                .build();
    }

    @ExceptionHandler(value = {
            MethodArgumentNotValidException.class,
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseRsp methodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException exception) {
        String message = exception.getAllErrors().stream().findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElseGet(BizCodeEnum.INVALID::getDefaultMsg);
        log.info("ResponseAdvice-methodArgumentNotValidException: {}", exception.getMessage());
        return BaseRsp.builder()
                .bizCode(BizCodeEnum.INVALID.getVal())
                .innerMsg(debugOpen ? exception.toString() : null)
                .message(message)
                .build();
    }

    @ExceptionHandler(value = {
            ConstraintViolationException.class,
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseRsp constraintViolationException(HttpServletRequest request, ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream().findFirst()
                .map(ConstraintViolation::getMessage)
                .orElseGet(BizCodeEnum.INVALID::getDefaultMsg);
        log.info("ResponseAdvice-constraintViolationException: {}", exception.getMessage());
        return BaseRsp.builder()
                .bizCode(BizCodeEnum.INVALID.getVal())
                .innerMsg(debugOpen ? exception.toString() : null)
                .message(message)
                .build();
    }

    @ExceptionHandler(value = {
            BindException.class,
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseRsp bindException(HttpServletRequest request, BindException exception) {
        String message = exception.getAllErrors().stream().findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElseGet(BizCodeEnum.INVALID::getDefaultMsg);
        log.info("ResponseAdvice-bindException: {}", exception.getMessage());
        return BaseRsp.builder()
                .bizCode(BizCodeEnum.INVALID.getVal())
                .innerMsg(debugOpen ? exception.toString() : null)
                .message(message)
                .build();
    }
}
