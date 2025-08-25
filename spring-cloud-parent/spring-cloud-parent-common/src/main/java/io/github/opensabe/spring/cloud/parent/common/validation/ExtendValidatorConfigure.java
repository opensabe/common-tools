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

import java.util.List;
import java.util.function.Consumer;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

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
@Import(ExtendValidatorConfigure.ConsumerConfig.class)
@AutoConfigureBefore(ValidationAutoConfiguration.class)
@AutoConfigureAfter(ExtendValidatorConfigure.ConsumerConfig.class)
@Configuration(proxyBeanMethods = false)
public class ExtendValidatorConfigure {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Primary
    public LocalValidatorFactoryBean validatorBean(ObjectProvider<List<Consumer<ConstraintMapping>>> consumerProvider) {
        var consumer = consumerProvider.getIfAvailable().stream().reduce(a -> {
        }, Consumer::andThen);
        var validation = new ValidatorFactoryBean(consumer);
        MessageInterpolatorFactory interpolatorFactory = new MessageInterpolatorFactory();
        validation.setMessageInterpolator(interpolatorFactory.getObject());
        return validation;
    }

    @AutoConfigureBefore(ExtendValidatorConfigure.class)
    @Configuration(proxyBeanMethods = false)
    public static class ConsumerConfig {
        @Bean
        public Consumer<ConstraintMapping> extendConstraint() {
            return mapping -> {
                mapping.constraintDefinition(NotBlank.class).validatedBy(ObjectBlankValidator.class);
                mapping.constraintDefinition(NotNull.class).validatedBy(StringNotNullValidator.class);
                mapping.constraintDefinition(IntegerEnumedValue.class).validatedBy(IntegerEnumedValidator.class);
            };
        }
    }


}
