package io.github.opensabe.spring.cloud.parent.webflux.common.config;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.cloud.parent.common.loadbalancer.TracedCircuitBreakerRoundRobinLoadBalancer;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.Resilience4jUtil;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.WebClientNamedContextFactory;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.resilience4j.retry.ClientResponseRetryOperator;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.resilience4j.ClientResponseCircuitBreakerOperator;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.actuate.metrics.web.reactive.client.ObservationWebClientCustomizer;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.CustomizedReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
@Configuration(proxyBeanMethods = false)
public class WebClientDefaultConfiguration {
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
        //如果没填写微服务名称，就使用配置 key 作为微服务名称
        if (StringUtils.isBlank(serviceName)) {
            serviceName = name;
        }
        String baseUrl = webClientProperties.getBaseUrl();
        //如果没填写 baseUrl，就使用微服务名称填充
        if (StringUtils.isBlank(baseUrl)) {
            baseUrl = "http://" + serviceName;
        }

        Retry retry = null;
        try {
            retry = retryRegistry.retry(serviceName, serviceName);
        } catch (ConfigurationNotFoundException e) {
            retry = retryRegistry.retry(serviceName);
        }
        //覆盖其中的异常判断
        retry = Retry.of(serviceName, RetryConfig.from(retry.getRetryConfig()).retryOnException(throwable -> {
            //WebClientResponseException 会重试，因为在这里能 catch 的 WebClientResponseException 只对可以重试的请求封装了 WebClientResponseException
            //参考 ClientResponseCircuitBreakerSubscriber 的代码
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
            //断路器异常重试，因为请求没有发出去
            if (throwable instanceof CallNotPermittedException) {
                log.info("should retry on {}", throwable.toString());
                return true;
            }
            if (throwable instanceof WebClientRequestException) {
                WebClientRequestException webClientRequestException = (WebClientRequestException) throwable;
                HttpMethod method = webClientRequestException.getMethod();
                URI uri = webClientRequestException.getUri();
                //判断是否为响应超时，响应超时代表请求已经发出去了，对于非 GET 并且没有标注可以重试的请求则不能重试
                boolean isResponseTimeout = false;
                Throwable cause = throwable.getCause();
                //netty 的读取超时一般是 ReadTimeoutException
                if (cause instanceof ReadTimeoutException) {
                    log.info("Cause is a ReadTimeoutException which indicates it is a response time out");
                    isResponseTimeout = true;
                } else {
                    //对于其他一些框架，使用了 java 底层 nio 的一般是 SocketTimeoutException，message 为 read time out
                    //还有一些其他异常，但是 message 都会有 read time out 字段，所以通过 message 判断
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
                //如果请求是 GET 或者标注了重试，则直接判断可以重试
                if (method.equals(HttpMethod.GET) || webClientProperties.retryablePathsMatch(uri.getPath())) {
                    log.info("should retry on {}-{}, {}", method, uri, throwable.toString());
                    return true;
                } else {
                    //否则，只针对请求还没有发出去的异常进行重试
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
        WebClient.Builder builder = WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                //最大 body 占用 16m 内存
                                .maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((clientRequest, exchangeFunction) -> {
                    //从 WebFlux 的 Context 中获取 observation，这个是框架塞入的
                    return Mono.deferContextual(contextView -> {
                        Observation observation = null;
                        if (contextView.hasKey(ObservationThreadLocalAccessor.KEY)) {
                            observation = contextView.get(ObservationThreadLocalAccessor.KEY);
                        } else {
                            //我们这里并不是想启动新的 Observation，只是想复用老的，如果老的不存在，其实是有问题的
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
                //Retry在负载均衡前
                .filter((clientRequest, exchangeFunction) -> {
                    return tracedFilter(clientRequest, () -> {
                        Optional<Object> attribute =
                                clientRequest.attribute(TracedCircuitBreakerRoundRobinLoadBalancer.OBSERVATION_KEY);
                        return exchangeFunction
                                .exchange(clientRequest)
                                //前面已经塞入 span，这里肯定存在
                                .transform(ClientResponseRetryOperator.of(finalRetry, (Observation) attribute.get()));
                    });
                })
                //负载均衡器，改写url
                .filter((clientRequest, exchangeFunction) -> {
                    return tracedFilter(clientRequest,
                            () -> lbFunction.filter(clientRequest, exchangeFunction)
                    );
                })
                //实例级别的断路器需要在负载均衡获取真正地址之后
                .filter((clientRequest, exchangeFunction) -> {
                    return tracedFilter(clientRequest,
                            () -> {
                                ServiceInstance serviceInstance = getServiceInstance(clientRequest);
                                CircuitBreaker circuitBreaker;
                                //这时候的url是经过负载均衡器的，是实例的url
                                //断路器是每个实例一个断路器
                                String instanceId = Resilience4jUtil.getServiceInstance(clientRequest.url().getHost(), clientRequest.url().getPort());
                                try {
                                    //使用实例id新建或者获取现有的CircuitBreaker,使用serviceName获取配置
                                    circuitBreaker = circuitBreakerRegistry.circuitBreaker(instanceId, finalServiceName);
                                } catch (ConfigurationNotFoundException e) {
                                    circuitBreaker = circuitBreakerRegistry.circuitBreaker(instanceId);
                                }
                                log.info("webclient circuit breaker [{}-{}] status: {}, data: {}", finalServiceName, instanceId, circuitBreaker.getState(), JsonUtil.toJSONString(circuitBreaker.getMetrics()));
                                return exchangeFunction.exchange(clientRequest).transform(ClientResponseCircuitBreakerOperator.of(circuitBreaker, serviceInstance, webClientProperties));
                            });
                }).baseUrl(baseUrl);
        //使用 observationWebClientCustomizer 定制化 builder，这样可以在链路自动添加 Observation 并且可以从 Context 获取
        observationWebClientCustomizer.customize(builder);
        return builder.build();
    }

    private static ServiceInstance getServiceInstance(ClientRequest clientRequest) {
        URI url = clientRequest.url();
        DefaultServiceInstance defaultServiceInstance = new DefaultServiceInstance();
        defaultServiceInstance.setHost(url.getHost());
        defaultServiceInstance.setPort(url.getPort());
        return defaultServiceInstance;
    }

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
}