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

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.parent.common.condition.ConditionOnSpringCloudConfigProfile;
import io.github.opensabe.spring.cloud.parent.common.handler.*;
import io.github.opensabe.spring.cloud.parent.common.web.Debug;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 拓展validation
 *
 * @author heng.ma
 */
@Configuration(proxyBeanMethods = false)
public class ExceptionConfiguration {


    @Bean
    @ConditionalOnMissingBean
    @ConditionOnSpringCloudConfigProfile("!online")
    public Debug test() {
        return new Debug(true);
    }
    @Bean
    @ConditionalOnMissingBean
    @ConditionOnSpringCloudConfigProfile("online")
    public Debug online() {
        return new Debug(false);
    }

    @Bean
    public ExceptionHandlerObservationAop exceptionHandlerObservationAop(UnifiedObservationFactory unifiedObservationFactory) {
        return new ExceptionHandlerObservationAop(unifiedObservationFactory);
    }

    @Bean
    public I18nMessageResolver i18nMessageResolver(MessageSource messageSource) {
        return new I18nMessageResolver(messageSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public GexceptionHandler gexceptionHandler(I18nMessageResolver i18nMessageResolver) {
        return new GexceptionHandler(i18nMessageResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ThrowableHandler throwableHandler(Debug debug) {
        return new ThrowableHandler(debug);
    }

    @Bean
    @ConditionalOnMissingBean
    public EnumConvertConfiguration enumConvertConfiguration() {
        return new EnumConvertConfiguration();
    }

}
