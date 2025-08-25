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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import feign.Feign;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import io.github.opensabe.spring.cloud.parent.web.common.feign.DefaultErrorDecoder;
import io.github.opensabe.spring.cloud.parent.web.common.feign.FeignDecoratorBuilderInterceptor;
import io.github.opensabe.spring.cloud.parent.web.common.feign.OpenfeignUtil;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

@Configuration(proxyBeanMethods = false)
public class DefaultOpenFeignConfiguration {

    private final String name;

    @Autowired
    public DefaultOpenFeignConfiguration(Environment environment) {
        this.name = OpenfeignUtil.getClientNamePropertyKey(environment);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new DefaultErrorDecoder();
    }

    @Bean
    public Feign.Builder resilience4jFeignBuilder(
            List<FeignDecoratorBuilderInterceptor> feignDecoratorBuilderInterceptors,
            FeignDecorators.Builder builder
    ) {
        feignDecoratorBuilderInterceptors.forEach(feignDecoratorBuilderInterceptor -> feignDecoratorBuilderInterceptor.intercept(builder));
        return Resilience4jFeign.builder(builder.build());
    }


    @Bean
    public FeignDecorators.Builder defaultBuilder() {
        return FeignDecorators.builder();
    }

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
