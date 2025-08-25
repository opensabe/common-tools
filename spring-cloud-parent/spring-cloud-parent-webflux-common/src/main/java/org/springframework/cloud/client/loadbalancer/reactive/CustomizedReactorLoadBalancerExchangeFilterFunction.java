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
package org.springframework.cloud.client.loadbalancer.reactive;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.CompletionContext;
import org.springframework.cloud.client.loadbalancer.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycle;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycleValidator;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;

import static org.springframework.cloud.client.loadbalancer.reactive.ExchangeFilterFunctionUtils.buildClientRequest;
import static org.springframework.cloud.client.loadbalancer.reactive.ExchangeFilterFunctionUtils.getHint;
import static org.springframework.cloud.client.loadbalancer.reactive.ExchangeFilterFunctionUtils.serviceInstanceUnavailableMessage;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * 本代码为下面代码的复制，由于重试放在 lb 之前，所以要保证每次复用 context，所以做了这个改造
 *
 * @see org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
 */
@Log4j2
public class CustomizedReactorLoadBalancerExchangeFilterFunction implements LoadBalancedExchangeFilterFunction {
    private final ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory;

    private final List<LoadBalancerClientRequestTransformer> transformers;
    /**
     * 不能使用并发不安全的 WeakHashMap
     * 需要使用 weakKeys，因为这里不确定何时 Request 会结束，需要保证不影响 Request 的垃圾回收
     * value 跟随 key 回收，value 不能使用 WeakReference
     */
    private final Cache<ClientRequest, DefaultRequest> requestRequestDataContextMap =
            Caffeine.newBuilder().weakKeys().weakValues()
                    //这里设置 1 小时，如果有因为超过时间过期的证明可能有内存泄漏
                    .expireAfterAccess(Duration.ofHours(1))
                    .removalListener((ClientRequest key, DefaultRequest value, RemovalCause cause) -> {
                        if (cause == RemovalCause.EXPIRED) {
                            log.warn("RequestDataContext was evicted, key: {}, value: {}, maybe memory leak", key, value);
                        }
                    })
                    .build();

    /**
     * @param loadBalancerFactory the loadbalancer factory
     * @param properties          the properties for SC LoadBalancer
     * @deprecated Deprecated in favor of
     * {@link #CustomizedReactorLoadBalancerExchangeFilterFunction(ReactiveLoadBalancer.Factory, LoadBalancerProperties, List)}.
     */
    @Deprecated
    public CustomizedReactorLoadBalancerExchangeFilterFunction(ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory,
                                                               LoadBalancerProperties properties) {
        this(loadBalancerFactory, properties, Collections.emptyList());
    }

    /**
     * @deprecated in favour of
     * {@link CustomizedReactorLoadBalancerExchangeFilterFunction#CustomizedReactorLoadBalancerExchangeFilterFunction(ReactiveLoadBalancer.Factory, List)}
     */
    @Deprecated
    public CustomizedReactorLoadBalancerExchangeFilterFunction(ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory,
                                                               LoadBalancerProperties properties, List<LoadBalancerClientRequestTransformer> transformers) {
        this.loadBalancerFactory = loadBalancerFactory;
        this.transformers = transformers;
    }

    public CustomizedReactorLoadBalancerExchangeFilterFunction(ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory,
                                                               List<LoadBalancerClientRequestTransformer> transformers) {
        this.loadBalancerFactory = loadBalancerFactory;
        this.transformers = transformers;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest clientRequest, ExchangeFunction next) {
        URI originalUrl = clientRequest.url();
        String serviceId = originalUrl.getHost();
        if (serviceId == null) {
            String message = String.format("Request URI does not contain a valid hostname: %s", originalUrl.toString());
            if (log.isWarnEnabled()) {
                log.warn(message);
            }
            return Mono.just(ClientResponse.create(HttpStatus.BAD_REQUEST).body(message).build());
        }
        Set<LoadBalancerLifecycle> supportedLifecycleProcessors = LoadBalancerLifecycleValidator
                .getSupportedLifecycleProcessors(
                        loadBalancerFactory.getInstances(serviceId, LoadBalancerLifecycle.class),
                        RequestDataContext.class, ResponseData.class, ServiceInstance.class);
        String hint = getHint(serviceId, loadBalancerFactory.getProperties(serviceId).getHint());
        //修改从这里开始：RequestDataContext 复用
        DefaultRequest<RequestDataContext> lbRequest =
                requestRequestDataContextMap.get(clientRequest, k -> {
                    return new DefaultRequest<>(new RequestDataContext(new RequestData(clientRequest), hint));
                });
        //修改结束
        supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStart(lbRequest));
        //修改原来的 choose，因为把 Retry 放在了之前，需要将 choose 的调用放入重试 Context
        //需要将原来的 choose 使用 Mono.defer 包装，否则重试的时候会一直重试相同的实例，不会重新调用 choose
        return Mono.defer(() ->
                choose(serviceId, lbRequest)
        ).flatMap(lbResponse -> {
            ServiceInstance instance = lbResponse.getServer();
            if (instance == null) {
                String message = serviceInstanceUnavailableMessage(serviceId);
                if (log.isWarnEnabled()) {
                    log.warn(message);
                }
                supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
                        .onComplete(new CompletionContext<>(CompletionContext.Status.DISCARD, lbRequest, lbResponse)));
                return Mono.just(ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(serviceInstanceUnavailableMessage(serviceId)).build());
            }

            if (log.isDebugEnabled()) {
                log.debug(String.format("LoadBalancer has retrieved the instance for service %s: %s", serviceId,
                        instance.getUri()));
            }
            LoadBalancerProperties.StickySession stickySessionProperties = loadBalancerFactory.getProperties(serviceId)
                    .getStickySession();
            ClientRequest newRequest = buildClientRequest(clientRequest, instance,
                    stickySessionProperties.getInstanceIdCookieName(),
                    stickySessionProperties.isAddServiceInstanceCookie(), transformers);
            supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStartRequest(lbRequest, lbResponse));
            return next.exchange(newRequest)
                    .doOnError(throwable -> supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
                            .onComplete(new CompletionContext<ResponseData, ServiceInstance, RequestDataContext>(
                                    CompletionContext.Status.FAILED, throwable, lbRequest, lbResponse))))
                    .doOnSuccess(clientResponse -> supportedLifecycleProcessors.forEach(
                            lifecycle -> lifecycle.onComplete(new CompletionContext<>(CompletionContext.Status.SUCCESS,
                                    lbRequest, lbResponse, new ResponseData(clientResponse, lbRequest.getContext().getClientRequest())))));
        });
    }

    protected Mono<Response<ServiceInstance>> choose(String serviceId, Request<RequestDataContext> request) {
        ReactiveLoadBalancer<ServiceInstance> loadBalancer = loadBalancerFactory.getInstance(serviceId);
        if (loadBalancer == null) {
            return Mono.just(new EmptyResponse());
        }
        return Mono.from(loadBalancer.choose(request));
    }

}
