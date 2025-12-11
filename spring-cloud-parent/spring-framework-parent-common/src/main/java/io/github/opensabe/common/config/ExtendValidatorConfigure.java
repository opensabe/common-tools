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
package io.github.opensabe.common.config;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

import io.github.opensabe.spring.cloud.parent.common.validation.IntegerEnumedValidator;
import io.github.opensabe.spring.cloud.parent.common.validation.ObjectBlankValidator;
import io.github.opensabe.spring.cloud.parent.common.validation.StringNotNullValidator;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

import io.github.opensabe.spring.cloud.parent.common.validation.annotation.IntegerEnumedValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 补充notBlank，强化notNull
 * <p>
 * 项目中有
 * {@link org.springframework.web.servlet.config.annotation.EnableWebMvc}
 * <p>
 * 又或者项目中有自定义的
 * {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport}
 * 子类
 * 需要覆盖getValidator方法，否则会用默认的validator
 * <p>
 * 为了解决这个问题，可以实现{@link org.springframework.web.servlet.config.annotation.WebMvcConfigurer}
 * 接口来替代父类
 * <p>
 * <b>Webflux同理</b>
 *
 * @author heng.ma
 */
public class ExtendValidatorConfigure {


    @Bean
    public Consumer<ConstraintMapping> extendConstraint() {
        return mapping -> {
            mapping.constraintDefinition(NotBlank.class).validatedBy(ObjectBlankValidator.class);
            mapping.constraintDefinition(NotNull.class).validatedBy(StringNotNullValidator.class);
            mapping.constraintDefinition(IntegerEnumedValue.class).validatedBy(IntegerEnumedValidator.class);
        };
    }

    @Bean
    public ValidationConfigurationCustomizer validationConfigurationCustomizer(ObjectProvider<List<Consumer<ConstraintMapping>>> consumerProvider) {
        return configuration -> {
            if (configuration instanceof ConfigurationImpl config) {
                var consumer = consumerProvider.getIfAvailable().stream().reduce(a -> {}, Consumer::andThen);
                var mapping = config.createConstraintMapping();
                if (Objects.nonNull(consumer)) {
                    consumer.accept(mapping);
                }
                config.addMapping(mapping);
                config.failFast(true);
                config.locales(Locale.ENGLISH);
            }
        };
    }


}
