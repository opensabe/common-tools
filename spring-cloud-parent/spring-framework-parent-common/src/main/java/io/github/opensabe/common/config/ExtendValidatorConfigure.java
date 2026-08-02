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
import java.util.function.Consumer;

import io.github.opensabe.spring.cloud.parent.common.validation.IntegerEnumedValidator;
import io.github.opensabe.spring.cloud.parent.common.validation.ObjectBlankValidator;
import io.github.opensabe.spring.cloud.parent.common.validation.StringNotNullValidator;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.springframework.boot.validation.autoconfigure.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

import io.github.opensabe.spring.cloud.parent.common.validation.annotation.IntegerEnumedValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 扩展 Hibernate Validator 约束映射的配置类。
 * <p>
 * 补充 {@link NotBlank}、强化 {@link NotNull}，并注册 {@link IntegerEnumedValue} 自定义校验器。
 * <p>
 * 若项目中使用了 {@link org.springframework.web.servlet.config.annotation.EnableWebMvc}，
 * 或自定义了 {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport}
 * 子类且覆盖了 {@code getValidator()}，则会回退到默认 Validator，本配置不会生效。
 * 此时应改用 {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurer} 接口。
 * <p>
 * <b>WebFlux 同理。</b>
 *
 * @author heng.ma
 */
public class ExtendValidatorConfigure {

    /**
     * 注册扩展约束与自定义 Validator 实现的映射关系。
     *
     * @return 向 {@link ConstraintMapping} 追加约束定义的 Consumer
     */
    @Bean
    public Consumer<ConstraintMapping> extendConstraint() {
        return mapping -> {
            mapping.constraintDefinition(NotBlank.class).validatedBy(ObjectBlankValidator.class);
            mapping.constraintDefinition(NotNull.class).validatedBy(StringNotNullValidator.class);
            mapping.constraintDefinition(IntegerEnumedValue.class).validatedBy(IntegerEnumedValidator.class);
        };
    }

    /**
     * 定制 Hibernate Validator 引擎：合并所有 {@link ConstraintMapping} 扩展、启用 fail-fast、固定英文 locale。
     *
     * @param consumers 容器中所有 {@link ConstraintMapping} 扩展 Consumer（可为空列表）
     * @return Validation 引擎定制器
     */
    @Bean
    public ValidationConfigurationCustomizer validationConfigurationCustomizer(List<Consumer<ConstraintMapping>> consumers) {
        return configuration -> {
            if (configuration instanceof ConfigurationImpl config) {
                Consumer<ConstraintMapping> consumer = consumers.stream()
                        .reduce(Consumer::andThen)
                        .orElse(c -> {});
                var mapping = config.createConstraintMapping();
                consumer.accept(mapping);
                config.addMapping(mapping);
                config.failFast(true);
                config.locales(Locale.ENGLISH);
            }
        };
    }

}
