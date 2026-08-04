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

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.webclient.observation.ObservationWebClientCustomizer;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.CustomizedReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.cloud.parent.common.loadbalancer.TracedCircuitBreakerRoundRobinLoadBalancer;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.Resilience4jUtil;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.WebClientNamedContextFactory;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.resilience4j.ClientResponseCircuitBreakerOperator;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.resilience4j.retry.ClientResponseRetryOperator;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * 命名 WebClient 默认配置。
 * <p>
 * 按 {@link WebClientNamedContextFactory} 上下文名称读取 {@link WebClientConfigurationProperties}，
 * 组装带负载均衡、重试、实例级断路器与 Observation 链路透传的 {@link WebClient}。
 */
@Log4j2
/**
 * 单个 WebClient 命名上下文的默认配置。
 * <p>
 * 注册 Resilience4j 断路器/重试 Operator 与 Observation 过滤器等。
 */
@Configuration(proxyBeanMethods = false)
public class WebClientDefaultConfiguration {

    /**
     * 从请求 URL 构造占位 {@link ServiceInstance}，供断路器 Operator 使用。
     *
     * @param clientRequest 当前客户端请求
     * @return 由 URL 主机与端口构成的服务实例
     */
    private static ServiceInstance getServiceInstance(ClientRequest clientRequest) {
        URI url = clientRequest.url();
        return new DefaultServiceInstance(null, null, url.getHost(), url.getPort(), false);
    }

    /**
     * 若请求携带 Observation 属性，则在 Observation 作用域内执行后续 Exchange。
     *
     * @param clientRequest 客户端请求
     * @param supplier      实际 Exchange 逻辑
     * @return 客户端响应 Mono
     */
    private static Mono<ClientResponse> tracedFilter(
            ClientRequest clientRequest,
            Supplier<Mono<ClientResponse>> supplier) {
        Optional<Object> attribute =
                clientRequest.attribute(TracedCircuitBreakerRoundRobinLoadBalancer.OBSERVATION_KEY);
        if (attribute.isPresent() && attribute.get() instanceof Observation observation) {
            return observation.scoped(supplier);
        } else {
            return supplier.get();
        }
    }

    /**
     * 按命名上下文创建配置完整的 {@link WebClient}。
     * <p>
     * 过滤器链顺序：Observation 注入 → 重试 → 负载均衡 → 实例级断路器。
     *
     * @param lbFunction                      负载均衡 Exchange 过滤器
     * @param applicationContext              应用上下文
     * @param webClientConfigurationProperties WebClient 配置属性
     * @param environment                     Spring 环境
     * @param retryRegistry                   Resilience4j 重试注册表
     * @param circuitBreakerRegistry          Resilience4j 断路器注册表
     * @param observationWebClientCustomizer  Observation 定制器
     * @param unifiedObservationFactory       统一 Observation 工厂
     * @return 配置完成的 WebClient
     */
    @Bean
    public WebClient getWebClient(
            CustomizedReactorLoadBalancerExchangeFilterFunction lbFunction,
            ConfigurableApplicationContext applicationContext,
            WebClientConfigurationProperties webClientConfigurationProperties,
            Environment environment,
            RetryRegistry retryRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry,
            ObservationWebClientCustomizer observationWebClientCustomizer,
            UnifiedObservationFactory unifiedObservationFactory
    ) {
        String name = environment.getProperty(WebClientNamedContextFactory.PROPERTY_NAME);
        Map<String, WebClientConfigurationProperties.WebClientProperties> configs = webClientConfigurationProperties.getConfigs();
        if (configs == null || configs.size() == 0) {
            throw new BeanCreationException("Failed to create webClient, please provide configurations under namespace: webclient.configs");
        }
        WebClientConfigurationProperties.WebClientProperties webClientProperties = configs.get(name);
        if (webClientProperties == null) {
            throw new BeanCreationException("Failed to create webClient, please provide configurations under namespace: webclient.configs." + name);
        }
        String serviceName = webClientProperties.getServiceName();
        // 若未填写微服务名称，则使用配置 key 作为微服务名称
        if (StringUtils.isBlank(serviceName)) {
            serviceName = name;
        }
        String baseUrl = webClientProperties.getBaseUrl();
        // 若未填写 baseUrl，则使用微服务名称填充
        if (StringUtils.isBlank(baseUrl)) {
            baseUrl = "http://" + serviceName;
        }

        Retry retry = null;
        try {
            retry = retryRegistry.retry(serviceName, serviceName);
        } catch (ConfigurationNotFoundException e) {
            retry = retryRegistry.retry(serviceName);
        }
        // 覆盖其中的异常判断逻辑
        retry = Retry.of(serviceName, RetryConfig.from(retry.getRetryConfig()).retryOnException(throwable -> {
            // WebClientResponseException 会重试，因为能 catch 到的 WebClientResponseException
            // 仅对可重试请求封装；参考 ClientResponseCircuitBreakerSubscriber
            if (throwable instanceof WebClientResponseException) {
                WebClientResponseException webClientResponseException = (WebClientResponseException) throwable;
                boolean isClientError = webClientResponseException.getStatusCode().is4xxClientError();
                if (isClientError) {
                    log.info("should not retry on client error {}", throwable.toString());
                    return false;
                }
                log.info("should retry on {}", throwable.toString());
                return true;
            }
            // 断路器拒绝时重试，因请求尚未发出
            if (throwable instanceof CallNotPermittedException) {
                log.info("should retry on {}", throwable.toString());
                return true;
            }
            if (throwable instanceof WebClientRequestException) {
                WebClientRequestException webClientRequestException = (WebClientRequestException) throwable;
                HttpMethod method = webClientRequestException.getMethod();
                URI uri = webClientRequestException.getUri();
                // 判断是否为响应超时：响应超时表示请求已发出，对非 GET 且未标注可重试的请求不应重试
                boolean isResponseTimeout = false;
                Throwable cause = throwable.getCause();
                // Netty 读取超时一般为 ReadTimeoutException
                if (cause instanceof ReadTimeoutException) {
                    log.info("Cause is a ReadTimeoutException which indicates it is a response time out");
                    isResponseTimeout = true;
                } else {
                    // 其他框架使用 Java NIO 时多为 SocketTimeoutException，message 含 read time out
                    String message = throwable.getMessage();
                    if (StringUtils.isNotBlank(message)) {
                        message = message.replace(" ", "");
                        if (StringUtils.containsIgnoreCase(message, "readtimeout")) {
                            log.info("Throwable message contains readtimeout which indicates it is a response time out: {}", throwable.getMessage());
                            isResponseTimeout = true;
                        }
                        if (StringUtils.containsIgnoreCase(message, "respon")) {
                            log.info("Throwable message contains respon which indicates it is sent {}", throwable.getMessage());
                            isResponseTimeout = true;
                        }
                    }
                }
                // GET 或路径匹配可重试配置时允许重试
                if (method.equals(HttpMethod.GET) || webClientProperties.retryablePathsMatch(uri.getPath())) {
                    log.info("should retry on {}-{}, {}", method, uri, throwable.toString());
                    return true;
                } else {
                    // 非 GET：仅对请求尚未发出的异常重试
                    if (isResponseTimeout) {
                        log.info("should not retry on {}-{}, {}", method, uri, throwable.toString());
                    } else {
                        log.info("should retry on {}-{}, {}", method, uri, throwable.toString());
                        return true;
                    }
                }
            }
            return false;
        }).build());


        HttpClient httpClient = HttpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) webClientProperties.getConnectTimeout().toMillis())
                .doOnConnected(connection ->
                        connection
                                .addHandlerLast(new ReadTimeoutHandler((int) webClientProperties.getResponseTimeout().toSeconds()))
                                .addHandlerLast(new WriteTimeoutHandler((int) webClientProperties.getResponseTimeout().toSeconds()))
                );

        Retry finalRetry = retry;
        String finalServiceName = serviceName;
        WebClient.Builder builder = getBuilder(lbFunction, circuitBreakerRegistry, unifiedObservationFactory, httpClient, finalRetry, finalServiceName, webClientProperties, baseUrl);
        // 使用 observationWebClientCustomizer 定制 builder，以便在链路中自动添加 Observation
        observationWebClientCustomizer.customize(builder);
        return builder.build();
    }

    /**
     * 组装 WebClient Builder，注册 Observation、重试、负载均衡与实例级断路器过滤器。
     *
     * @param lbFunction                 负载均衡过滤器
     * @param circuitBreakerRegistry     断路器注册表
     * @param unifiedObservationFactory  Observation 工厂
     * @param httpClient                 Reactor Netty HTTP 客户端
     * @param finalRetry                 重试策略
     * @param finalServiceName           服务名（用于断路器配置查找）
     * @param webClientProperties        当前 WebClient 属性
     * @param baseUrl                    基础 URL
     * @return 已配置过滤器的 WebClient Builder
     */
    private static WebClient.Builder getBuilder(
            CustomizedReactorLoadBalancerExchangeFilterFunction lbFunction,
            CircuitBreakerRegistry circuitBreakerRegistry,
            UnifiedObservationFactory unifiedObservationFactory,
            HttpClient httpClient, Retry finalRetry,
            String finalServiceName,
            WebClientConfigurationProperties.WebClientProperties webClientProperties, String baseUrl
    ) {
        WebClient.Builder builder = WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                // 最大 body 占用 16MB 内存
                                .maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((clientRequest, exchangeFunction) -> {
                    // 从 WebFlux Context 获取 Observation（由框架注入）
                    return Mono.deferContextual(contextView -> {
                        Observation observation = null;
                        if (contextView.hasKey(ObservationThreadLocalAccessor.KEY)) {
                            observation = contextView.get(ObservationThreadLocalAccessor.KEY);
                        } else {
                            // 复用已有 Observation；若不存在则创建空 Observation（通常表示上下文缺失）
                            observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
                        }
                        ClientRequest.Builder clientBuilder = ClientRequest.from(clientRequest);
                        clientBuilder.attribute(
                                TracedCircuitBreakerRoundRobinLoadBalancer.OBSERVATION_KEY,
                                observation
                        );
                        return exchangeFunction.exchange(clientBuilder.build());
                    });
                })
                // 重试在负载均衡之前
                .filter((clientRequest, exchangeFunction) -> {
                    return tracedFilter(clientRequest, () -> {
                        Optional<Object> attribute =
                                clientRequest.attribute(TracedCircuitBreakerRoundRobinLoadBalancer.OBSERVATION_KEY);
                        return exchangeFunction
                                .exchange(clientRequest)
                                .transform(ClientResponseRetryOperator.of(finalRetry, (Observation) attribute.get()));
                    });
                })
                // 负载均衡器，改写 URL
                .filter((clientRequest, exchangeFunction) -> {
                    return tracedFilter(clientRequest,
                            () -> lbFunction.filter(clientRequest, exchangeFunction)
                    );
                })
                // 实例级断路器在负载均衡解析真实地址之后
                .filter((clientRequest, exchangeFunction) -> {
                    return tracedFilter(clientRequest,
                            () -> {
                                ServiceInstance serviceInstance = getServiceInstance(clientRequest);
                                CircuitBreaker circuitBreaker;
                                // 此时 URL 已由负载均衡器改写为实例地址；每个实例一个断路器
                                String instanceId = Resilience4jUtil.getServiceInstance(clientRequest.url().getHost(), clientRequest.url().getPort());
                                try {
                                    circuitBreaker = circuitBreakerRegistry.circuitBreaker(instanceId, finalServiceName);
                                } catch (ConfigurationNotFoundException e) {
                                    circuitBreaker = circuitBreakerRegistry.circuitBreaker(instanceId);
                                }
                                log.info("webclient circuit breaker [{}-{}] status: {}, data: {}", finalServiceName, instanceId, circuitBreaker.getState(), JsonUtil.toJSONString(circuitBreaker.getMetrics()));
                                return exchangeFunction.exchange(clientRequest).transform(ClientResponseCircuitBreakerOperator.of(circuitBreaker, serviceInstance, webClientProperties));
                            });
                }).baseUrl(baseUrl);
        return builder;
    }
}
