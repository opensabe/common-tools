package io.github.opensabe.spring.cloud.parent.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;



public class StringNotNullValidator implements ConstraintValidator<NotNull,String> {

    @Override
    public boolean isValid(String object, ConstraintValidatorContext constraintValidatorContext) {
        return StringUtils.isNotBlank(object);
    }
}
