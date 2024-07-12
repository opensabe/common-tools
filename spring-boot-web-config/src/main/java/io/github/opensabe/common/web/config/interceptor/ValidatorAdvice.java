package io.github.opensabe.common.web.config.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.util.Collection;

@ControllerAdvice
public class ValidatorAdvice {

    @Autowired
    protected LocalValidatorFactoryBean validator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        Object target = binder.getTarget();
        if (target != null) {
            Boolean isCo = Collection.class.isAssignableFrom(target.getClass());
            if (isCo) {
                binder.addValidators(new CollectionValidator(validator));
            }
        }
    }

}
