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

import java.util.Collection;

import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

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
