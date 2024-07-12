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
