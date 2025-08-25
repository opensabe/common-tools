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
package io.github.opensabe.spring.cloud.parent.common.validation;

import jakarta.annotation.Nullable;
import jakarta.validation.Configuration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public class ValidatorFactoryBean extends LocalValidatorFactoryBean {

    private final Consumer<ConstraintMapping> mappingConsumer;

    public ValidatorFactoryBean(Consumer<ConstraintMapping> mappingConsumer) {
        this.mappingConsumer = mappingConsumer;
    }

    /**
     * Post-process the given Bean Validation configuration,
     * adding to or overriding any of its settings.
     * <p>Invoked right before building the {@link jakarta.validation.ValidatorFactory}.
     * @param configuration the Configuration object, pre-populated with
     * settings driven by LocalValidatorFactoryBean's properties
     */
    protected void postProcessConfiguration(@Nullable Configuration<?> configuration) {
        if (configuration instanceof ConfigurationImpl config) {
            var mapping = config.createConstraintMapping();
            if (Objects.nonNull(mappingConsumer)) {
                mappingConsumer.accept(mapping);
            }
            config.addMapping(mapping);
            config.failFast(true);
            config.locales(Locale.ENGLISH);
        }
    }
}
