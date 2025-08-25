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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
public abstract class ErrorHandler {
    private final ParserContext parserContext = new TemplateParserContext("${","}");
    private final SpelExpressionParser parser = new SpelExpressionParser();


    protected String message (BindingResult bindingResult) {
        return Optional.ofNullable(bindingResult)
                .map(BindingResult::getFieldErrors)
                .stream().flatMap(List::stream)
                .map(this::getMessage)
                .collect(Collectors.joining(","));
    }

    private String getMessage (FieldError error) {
        var source = error.unwrap(ConstraintViolation.class);
        if (Objects.nonNull(source)) {
            if (source.getMessageTemplate().startsWith("{")) {
                return error.getField() + " " +error.getDefaultMessage();
            }
        }
        return parser.parseExpression(error.getDefaultMessage(),parserContext).getValue(error,String.class);
    }

    public BaseRsp<Void> constraintViolationException (ConstraintViolationException e) {
        return BaseRsp.<Void>builder().bizCode(BizCodeEnum.INVALID.getVal()).message(e.getMessage()).build();
    }

    public abstract BaseRsp onFrontendException (FrontendException e);
    public abstract BaseRsp onBackendException (BackendException e);
    public abstract BaseRsp onConstraintViolationException (ConstraintViolationException e);
    public abstract BaseRsp onBindException (BindException e);
}
