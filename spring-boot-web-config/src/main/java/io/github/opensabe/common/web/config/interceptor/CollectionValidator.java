package io.github.opensabe.common.web.config.interceptor;

import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Collection;

public class CollectionValidator implements Validator {
    private final Validator validator;

    public CollectionValidator(LocalValidatorFactoryBean validatorFactory) {
        this.validator = validatorFactory;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Collection.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Collection collection = (Collection) o;
        if (CollectionUtils.isEmpty(collection)) {
            errors.reject("The collection parameter can't be empty!");
        } else {
            for (Object object : collection) {
                ValidationUtils.invokeValidator(validator, object, errors);
            }
        }
    }
}
