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
package io.github.opensabe.spring.cloud.parent.webflux.common.webclient.resilience4j;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.UnknownHttpStatusCodeException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static java.util.Objects.requireNonNull;

import io.github.opensabe.common.utils.AlarmUtil;
import io.github.opensabe.spring.cloud.parent.webflux.common.config.WebClientConfigurationProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.AbstractSubscriber;
import lombok.extern.log4j.Log4j2;
import reactor.core.CoreSubscriber;

/**
 * 基于官方的 CircuitBreakerOperator 针对 CircuitBreakerSubscriber 改造，基于 ClientResponse 的 http status code
 *
 * @see io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerSubscriber
 */
@Log4j2
public class ClientResponseCircuitBreakerSubscriber extends AbstractSubscriber<ClientResponse> {
    private static final byte[] EMPTY = new byte[0];
    private static final Class<?> aClass;
    private static final Method request;

    static {
        try {
            aClass = Class.forName("org.springframework.web.reactive.function.client.DefaultClientResponse");
            request = ReflectionUtils.findMethod(aClass, "request");
            request.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final CircuitBreaker circuitBreaker;
    private final ServiceInstance serviceInstance;
    private final WebClientConfigurationProperties.WebClientProperties webClientProperties;
    private final long start;
    private final boolean singleProducer;
    private final AtomicBoolean successSignaled = new AtomicBoolean(false);
    private final AtomicBoolean eventWasEmitted = new AtomicBoolean(false);


    protected ClientResponseCircuitBreakerSubscriber(
            CircuitBreaker circuitBreaker,
            CoreSubscriber<? super ClientResponse> downstreamSubscriber,
            ServiceInstance serviceInstance, boolean singleProducer,
            WebClientConfigurationProperties.WebClientProperties webClientProperties) {
        super(downstreamSubscriber);
        this.circuitBreaker = requireNonNull(circuitBreaker);
        this.serviceInstance = serviceInstance;
        this.singleProducer = singleProducer;
        this.start = circuitBreaker.getCurrentTimestamp();
        this.webClientProperties = webClientProperties;
    }

    @Override
    protected void hookOnNext(ClientResponse clientResponse) {
        if (!isDisposed()) {
            if (singleProducer && successSignaled.compareAndSet(false, true)) {
                int rawStatusCode = clientResponse.statusCode().value();
                HttpStatus httpStatus = HttpStatus.resolve(rawStatusCode);
                try {
                    HttpRequest httpRequest = (HttpRequest) request.invoke(clientResponse);
                    //判断方法是否为 GET，以及是否在可重试路径配置中，从而得出是否可以重试
                    if (httpRequest.getMethod() != HttpMethod.GET && !webClientProperties.retryablePathsMatch(httpRequest.getURI().getPath())) {
                        if (Objects.equals(httpStatus, HttpStatus.SERVICE_UNAVAILABLE)) {
                            //503 是负载均衡器或者调用的服务优雅关闭的时候返回的，应该重试
                            Exception exception = WebClientResponseException.create(rawStatusCode, httpStatus.getReasonPhrase(), clientResponse.headers().asHttpHeaders(), EMPTY, null, null);
                            circuitBreaker.onError(circuitBreaker.getCurrentTimestamp() - start, circuitBreaker.getTimestampUnit(), exception);
                            downstreamSubscriber.onError(exception);
                            return;
                        }
                        //如果不能重试，则直接返回结果
                        circuitBreaker.onResult(circuitBreaker.getCurrentTimestamp() - start, circuitBreaker.getTimestampUnit(), clientResponse);
                    } else {
                        if (httpStatus != null && httpStatus.is2xxSuccessful()) {
                            //如果成功，则直接返回结果
                            circuitBreaker.onResult(circuitBreaker.getCurrentTimestamp() - start, circuitBreaker.getTimestampUnit(), clientResponse);
                        } else {
                            /**
                             * 如果异常，参考 DefaultClientResponse 的代码进行异常封装
                             * @see org.springframework.web.reactive.function.client.DefaultClientResponse#createException
                             */
                            Exception exception;
                            if (httpStatus != null) {
                                exception = WebClientResponseException.create(rawStatusCode, httpStatus.getReasonPhrase(), clientResponse.headers().asHttpHeaders(), EMPTY, null, null);
                            } else {
                                exception = new UnknownHttpStatusCodeException(rawStatusCode, clientResponse.headers().asHttpHeaders(), EMPTY, null, null);
                            }
                            circuitBreaker.onError(circuitBreaker.getCurrentTimestamp() - start, circuitBreaker.getTimestampUnit(), exception);
                            downstreamSubscriber.onError(exception);
                            return;
                        }
                    }
                } catch (Exception e) {
                    AlarmUtil.fatal("judge request method in circuit breaker error! the resilience4j feature would not be enabled: {}", e.getMessage(), e);
                    circuitBreaker.onResult(circuitBreaker.getCurrentTimestamp() - start, circuitBreaker.getTimestampUnit(), clientResponse);
                }
            }
            eventWasEmitted.set(true);
            downstreamSubscriber.onNext(clientResponse);
        }
    }

    @Override
    protected void hookOnComplete() {
        if (successSignaled.compareAndSet(false, true)) {
            circuitBreaker.onSuccess(circuitBreaker.getCurrentTimestamp() - start, circuitBreaker.getTimestampUnit());
        }

        downstreamSubscriber.onComplete();
    }

    @Override
    public void hookOnCancel() {
        if (!successSignaled.get()) {
            if (eventWasEmitted.get()) {
                circuitBreaker.onSuccess(circuitBreaker.getCurrentTimestamp() - start, circuitBreaker.getTimestampUnit());
            } else {
                circuitBreaker.releasePermission();
            }
        }
    }

    @Override
    protected void hookOnError(Throwable e) {
        circuitBreaker.onError(circuitBreaker.getCurrentTimestamp() - start, circuitBreaker.getTimestampUnit(), e);
        downstreamSubscriber.onError(e);
    }
}
