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
 * 全局异常处理与调试开关相关的 Spring 配置。
 * <p>
 * 按 Spring Cloud Config profile 区分线上/非线上环境的 {@link Debug} 行为，
 * 并注册统一异常处理器、国际化消息解析与枚举转换等组件。
 *
 * @author heng.ma
 */
@Configuration(proxyBeanMethods = false)
public class ExceptionConfiguration {

    /**
     * 非线上环境（{@code spring.cloud.config.profile} 不包含 {@code online}）启用调试模式。
     *
     * @return 开启调试的 {@link Debug} 实例
     */
    @Bean
    @ConditionalOnMissingBean(Debug.class)
    @ConditionOnSpringCloudConfigProfile(value = "!online", predicate = ConditionOnSpringCloudConfigProfile.Predicate.contains)
    public Debug test() {
        return new Debug(true);
    }

    /**
     * 线上环境（profile 包含 {@code online}，如 {@code online}、{@code au-online}、{@code us-online}）关闭调试模式。
     *
     * @return 关闭调试的 {@link Debug} 实例
     */
    @Bean
    @ConditionalOnMissingBean(Debug.class)
    @ConditionOnSpringCloudConfigProfile(value = "online", predicate = ConditionOnSpringCloudConfigProfile.Predicate.contains)
    public Debug online() {
        return new Debug(false);
    }

    /**
     * 为全局异常处理器注入观测切面，记录异常处理过程的 Observation。
     *
     * @param unifiedObservationFactory 延迟初始化的 Observation 工厂
     * @return 异常处理观测 AOP 切面
     */
    @Bean
    public ExceptionHandlerObservationAop exceptionHandlerObservationAop(UnifiedObservationFactory unifiedObservationFactory) {
        return new ExceptionHandlerObservationAop(unifiedObservationFactory);
    }

    /**
     * 基于 {@link MessageSource} 解析国际化错误消息。
     *
     * @param messageSource Spring 消息源
     * @return 国际化消息解析器
     */
    @Bean
    public I18nMessageResolver i18nMessageResolver(MessageSource messageSource) {
        return new I18nMessageResolver(messageSource);
    }

    /**
     * 注册全局业务异常处理器；应用未自定义时生效。
     *
     * @param i18nMessageResolver 国际化消息解析器
     * @return 全局异常处理器
     */
    @Bean
    @ConditionalOnMissingBean(GexceptionHandler.class)
    public GexceptionHandler gexceptionHandler(I18nMessageResolver i18nMessageResolver) {
        return new GexceptionHandler(i18nMessageResolver);
    }

    /**
     * 注册兜底 {@link Throwable} 处理器；应用未自定义时生效。
     *
     * @param debug 调试开关，控制是否向客户端暴露详细堆栈
     * @return 兜底异常处理器
     */
    @Bean
    @ConditionalOnMissingBean(ThrowableHandler.class)
    public ThrowableHandler throwableHandler(Debug debug) {
        return new ThrowableHandler(debug);
    }

    /**
     * 注册枚举参数转换配置；应用未自定义时生效。
     *
     * @return 枚举转换配置
     */
    @Bean
    @ConditionalOnMissingBean(EnumConvertConfiguration.class)
    public EnumConvertConfiguration enumConvertConfiguration() {
        return new EnumConvertConfiguration();
    }

}
