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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

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
