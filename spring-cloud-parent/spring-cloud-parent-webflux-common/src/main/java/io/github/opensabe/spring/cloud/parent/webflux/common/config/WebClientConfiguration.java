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
package io.github.opensabe.spring.cloud.parent.webflux.common.config;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.CustomizedReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerClientRequestTransformer;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.spring.cloud.parent.common.redislience4j.CircuitBreakerExtractor;
import io.github.opensabe.spring.cloud.parent.webflux.common.jfr.HttpServerJFRProperties;
import io.github.opensabe.spring.cloud.parent.webflux.common.jfr.HttpServerRequestObservationToJFRGenerator;
import io.github.opensabe.spring.cloud.parent.webflux.common.jfr.WebClientJFRConfigurationProperties;
import io.github.opensabe.spring.cloud.parent.webflux.common.jfr.WebClientObservationToJFRGenerator;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.WebClientNamedContextFactory;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.WebClientRequestCircuitBreakerExtractor;

/**
 * WebClient 通用配置。
 * <p>
 * 注册命名上下文工厂、断路器提取器、负载均衡过滤器与 JFR 观测生成器。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
        WebClientConfigurationProperties.class, WebClientJFRConfigurationProperties.class, HttpServerJFRProperties.class
})
public class WebClientConfiguration {
    /** @return WebClient 命名上下文工厂 */
    @Bean
    public WebClientNamedContextFactory getWebClientNamedContextFactory() {
        return new WebClientNamedContextFactory();
    }

    /** @return WebClient 断路器提取器 */
    @Bean
    public CircuitBreakerExtractor webClientRequestCircuitBreakerExtractor() {
        return new WebClientRequestCircuitBreakerExtractor();
    }

    /**
     * 注册带断路器感知的 Reactor 负载均衡 Exchange 过滤器。
     *
     * @param loadBalancerFactory 响应式负载均衡工厂
     * @param transformers        请求转换器（可选）
     * @return 负载均衡过滤器
     */
    @Bean
    public CustomizedReactorLoadBalancerExchangeFilterFunction customizedReactorLoadBalancerExchangeFilterFunction(
            ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory,
            ObjectProvider<List<LoadBalancerClientRequestTransformer>> transformers
    ) {
        return new CustomizedReactorLoadBalancerExchangeFilterFunction(
                loadBalancerFactory, transformers.getIfAvailable()
        );
    }

    /** @return WebClient JFR 观测生成器 */
    @Bean
    public WebClientObservationToJFRGenerator webClientObservationToJFRGenerator(
            WebClientJFRConfigurationProperties properties
    ) {
        return new WebClientObservationToJFRGenerator(properties);
    }

    /** @return WebFlux HTTP 服务器 JFR 观测生成器 */
    @Bean
    public HttpServerRequestObservationToJFRGenerator httpServerRequestObservationToJFRGenerator(
            HttpServerJFRProperties properties
    ) {
        return new HttpServerRequestObservationToJFRGenerator(properties);
    }
}
