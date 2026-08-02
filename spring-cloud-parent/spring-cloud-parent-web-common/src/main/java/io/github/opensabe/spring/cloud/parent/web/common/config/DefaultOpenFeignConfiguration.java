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
package io.github.opensabe.spring.cloud.parent.web.common.config;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import feign.Feign;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import io.github.opensabe.spring.cloud.parent.web.common.feign.DefaultErrorDecoder;
import io.github.opensabe.spring.cloud.parent.web.common.feign.FeignDecoratorBuilderInterceptor;
import io.github.opensabe.spring.cloud.parent.web.common.feign.OpenfeignUtil;
import io.github.opensabe.spring.cloud.parent.web.common.feign.ThreadSafeFeignHttpMessageConverters;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

/**
 * OpenFeign 默认客户端配置。
 * <p>
 * 由 {@link CommonOpenFeignConfiguration} 注入到 Feign 默认上下文，提供错误解码、
 * 线程安全消息转换器、Resilience4j 装饰器与重试策略等 Bean。
 */
@Configuration(proxyBeanMethods = false)
public class DefaultOpenFeignConfiguration {

    /**
     * 当前 Feign 客户端在 Resilience4j 注册表中的名称键。
     */
    private final String name;

    /**
     * 从环境变量解析 Feign 客户端名称，供 Resilience4j 组件查找配置。
     *
     * @param environment Spring 环境
     */
    @Autowired
    public DefaultOpenFeignConfiguration(Environment environment) {
        this.name = OpenfeignUtil.getClientNamePropertyKey(environment);
    }

    /**
     * 注册默认 Feign 错误解码器。
     *
     * @return {@link DefaultErrorDecoder} 实例
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new DefaultErrorDecoder();
    }

    /**
     * 注册线程安全的 {@link FeignHttpMessageConverters}，替代 OpenFeign 默认实现。
     * <p>
     * Boot 4 / OpenFeign 5 下，默认实现在并发首次解码时可能暴露空的转换器列表，
     * 导致 {@code 'messageConverters' must not be empty} 异常。
     *
     * @param customizers      Boot HTTP 消息转换器定制器
     * @param cloudCustomizers OpenFeign 消息转换器定制器
     * @return 线程安全的 Feign 消息转换器
     */
    @Bean
    @Primary
    public FeignHttpMessageConverters feignHttpMessageConverters(
            ObjectProvider<ClientHttpMessageConvertersCustomizer> customizers,
            ObjectProvider<HttpMessageConverterCustomizer> cloudCustomizers) {
        return new ThreadSafeFeignHttpMessageConverters(customizers, cloudCustomizers);
    }

    /**
     * 构建集成 Resilience4j 装饰器的 {@link Feign.Builder}。
     * <p>
     * 先经由所有 {@link FeignDecoratorBuilderInterceptor} 增强装饰器，再交给
     * {@link Resilience4jFeign} 生成最终 Builder。
     *
     * @param feignDecoratorBuilderInterceptors 装饰器拦截器列表
     * @param builder                         Resilience4j 装饰器构建器
     * @return 配置完成的 Feign Builder
     */
    @Bean
    public Feign.Builder resilience4jFeignBuilder(
            List<FeignDecoratorBuilderInterceptor> feignDecoratorBuilderInterceptors,
            FeignDecorators.Builder builder
    ) {
        feignDecoratorBuilderInterceptors.forEach(feignDecoratorBuilderInterceptor -> feignDecoratorBuilderInterceptor.intercept(builder));
        return Resilience4jFeign.builder(builder.build());
    }

    /**
     * 提供空的 Resilience4j {@link FeignDecorators} 构建器，供拦截器按需追加组件。
     *
     * @return 默认 FeignDecorators 构建器
     */
    @Bean
    public FeignDecorators.Builder defaultBuilder() {
        return FeignDecorators.builder();
    }

    /**
     * 根据 Resilience4j 重试配置创建 Feign 默认 {@link Retryer}。
     * <p>
     * 优先按 {@code name} 与配置名双键查找 {@link Retry}，找不到时回退为单键查找。
     *
     * @param environment   Spring 环境
     * @param retryRegistry Resilience4j 重试注册表
     * @return 间隔 500ms–1000ms、最大次数取自配置的 Retryer
     */
    @Bean
    public Retryer defaultRetryer(
            Environment environment,
            RetryRegistry retryRegistry
    ) {
        Retry retry = null;
        try {
            retry = retryRegistry.retry(name, name);
        } catch (ConfigurationNotFoundException e) {
            retry = retryRegistry.retry(name);
        }
        RetryConfig retryConfig = retry.getRetryConfig();
        return new Retryer.Default(500L, 1000L, retryConfig.getMaxAttempts());
    }


//    @Bean
//    public FeignDecoratorBuilderInterceptor retryInterceptor (RetryRegistry registry) {
//        return builder -> builder.withRetry(registry.retry(name));
//    }
//    @Bean
//    public FeignDecoratorBuilderInterceptor circuitBreakerInterceptor (CircuitBreakerRegistry registry) {
//        return builder -> builder.withCircuitBreaker(registry.circuitBreaker(name));
//    }


}
