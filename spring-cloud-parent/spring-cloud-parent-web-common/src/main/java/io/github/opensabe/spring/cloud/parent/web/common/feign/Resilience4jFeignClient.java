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
package io.github.opensabe.spring.cloud.parent.web.common.feign;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.Resilience4jUtil;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.ThreadPoolBulkHeadDecorated;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.ThreadPoolBulkHeadDecorator;
import io.github.opensabe.spring.cloud.parent.web.common.misc.SpecialHttpStatus;
import feign.Client;
import feign.Request;
import feign.Response;
import feign.httpclient.ApacheHttpClient;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.micrometer.observation.Observation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

@Slf4j
public class Resilience4jFeignClient implements Client {
    private final ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry;
    private final ThreadPoolBulkHeadDecorator threadPoolBulkHeadDecorator;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final UnifiedObservationFactory unifiedObservationFactory;
    private ApacheHttpClient apacheHttpClient;


    public Resilience4jFeignClient(
            ApacheHttpClient apacheHttpClient,
            ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry,
            ThreadPoolBulkHeadDecorator threadPoolBulkHeadDecorator,
            CircuitBreakerRegistry circuitBreakerRegistry,
            UnifiedObservationFactory unifiedObservationFactory
    ) {
        this.apacheHttpClient = apacheHttpClient;
        this.threadPoolBulkheadRegistry = threadPoolBulkheadRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.threadPoolBulkHeadDecorator = threadPoolBulkHeadDecorator;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        //目前这个改动主要用于从feignClient 的proxy实体中获取其contextId 而不是从FeignClient的接口中的declaredMethod中获取contextId，原因是
        //需要预热的FeignClient的extends的预热接口的方法没有@FeignClient 这样会报错annotation is null, contextId cannot been found 详情参考FeignPreheatingBase
        FeignClient annotation =request.requestTemplate().feignTarget().type().getAnnotation(FeignClient.class);
//        FeignClient annotation = request.requestTemplate().methodMetadata().method().getDeclaringClass().getAnnotation(FeignClient.class);
        //和 Retry 保持一致，使用 contextId，而不是微服务名称
        String contextId = annotation.contextId();
        //获取实例唯一id
        String serviceInstanceId = getServiceInstanceId(request);
        //获取实例+方法唯一id
        String serviceInstanceMethodId = getServiceInstanceMethodId(request);


        String threadPoolBulkHeadName = contextId + ":" + serviceInstanceId;

        ThreadPoolBulkhead threadPoolBulkhead;
        try {
            //每个实例一个线程池
            threadPoolBulkhead = threadPoolBulkheadRegistry.bulkhead(threadPoolBulkHeadName, contextId);
        } catch (ConfigurationNotFoundException e) {
            threadPoolBulkhead = threadPoolBulkheadRegistry.bulkhead(threadPoolBulkHeadName);
        }
        //确认是否被包装了，如果已经包装就不用再包装了
        if (!(threadPoolBulkhead instanceof ThreadPoolBulkHeadDecorated)) {
            if (threadPoolBulkHeadDecorator != null) {
                //防止并发导致包装多个
                synchronized (this) {
                    //再读取下，因为之前已经 bulkhead 读取过 threadPoolBulkheadRegistry，这里使用 name 获取一定是获取的已生成的，所以不用再传入 contextId 了
                    //再读取下确保真的没有并发包装过
                    threadPoolBulkhead = threadPoolBulkheadRegistry.bulkhead(threadPoolBulkHeadName);
                    if (!(threadPoolBulkhead instanceof ThreadPoolBulkHeadDecorated)) {
                        log.info("Resilience4jFeignClient-execute: decorate: {}", threadPoolBulkHeadName);
                        threadPoolBulkhead = threadPoolBulkHeadDecorator.decorate(threadPoolBulkhead);
                        ThreadPoolBulkHeadDecorated threadPoolBulkHeadDecorated = new ThreadPoolBulkHeadDecorated(threadPoolBulkhead);
                        threadPoolBulkheadRegistry.replace(threadPoolBulkHeadName, threadPoolBulkHeadDecorated);
                    }
                }
            }
        }

        CircuitBreaker circuitBreaker;
        try {
            //每个服务实例具体方法一个resilience4j熔断记录器，在服务实例具体方法维度做熔断，所有这个服务的实例具体方法共享这个服务的resilience4j熔断配置
            circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceInstanceMethodId, contextId);
        } catch (ConfigurationNotFoundException e) {
            circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceInstanceMethodId);
        }
        //我们这里并不是想启动新的 Observation，只是想复用老的，如果老的不存在，其实是有问题的
        //保持traceId
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        CircuitBreaker finalCircuitBreaker = circuitBreaker;
        ThreadPoolBulkhead finalThreadPoolBulkhead = threadPoolBulkhead;
        Supplier<CompletionStage<Response>> completionStageSupplier = ThreadPoolBulkhead.decorateSupplier(threadPoolBulkhead,
//                () -> {
                OpenfeignUtil.decorateSupplier(finalCircuitBreaker, () -> {
                    ThreadPoolBulkhead.Metrics metrics = finalThreadPoolBulkhead.getMetrics();
                    return observation.scoped(() -> {
                        try {
                            log.info("call url: {} -> {}, ThreadPoolStats(threads: {}, queue depth: {}): {}",
                                    request.httpMethod(),
                                    request.url(),
                                    finalThreadPoolBulkhead.getName(),
                                    metrics.getThreadPoolSize(),
                                    metrics.getQueueDepth()
                            );
                            Response execute = apacheHttpClient.execute(request, options);
                            log.info("response: {} - {}", execute.status(), execute.reason());
                            return execute;
                        } catch (IOException e) {
                            throw new CompletionException(e);
                        }
                    });
                })
//        }
        );

        try {
            Response response = completionStageSupplier.get().toCompletableFuture().join();
            return response;
        } catch (BulkheadFullException e) {
            return Response.builder()
                    .request(request)
                    .status(SpecialHttpStatus.BULKHEAD_FULL.getValue())
                    .reason(e.getLocalizedMessage())
                    .requestTemplate(request.requestTemplate()).build();
        } catch (CompletionException e) {
            //内部抛出的所有异常都被封装了一层 CompletionException，所以这里需要取出里面的 Exception
            Throwable cause = e.getCause();
            //对于断路器打开，返回对应特殊的错误码
            if (cause instanceof CallNotPermittedException) {
                return Response.builder()
                        .request(request)
                        .status(SpecialHttpStatus.CIRCUIT_BREAKER_ON.getValue())
                        .reason(cause.getLocalizedMessage())
                        .requestTemplate(request.requestTemplate()).build();
            }
            //对于 IOException，需要判断是否请求已经发送出去了
            //对于 connect time out 的异常，则可以重试，因为请求没发出去，但是例如 read time out 还有 connection reset 则不行，因为请求已经发出去了
            if (cause instanceof IOException) {
                String message = cause.getMessage().toLowerCase();
                boolean containsRead = message.contains("read") || message.contains("respon");
                if (containsRead) {
                    log.info("{}-{} exception contains read, which indicates the request has been sent", e.getMessage(), cause.getMessage());
                    //如果是 read 异常，则代表请求已经发了出去，则不能重试（除非是 GET 请求或者有 RetryableMethod 注解，这个在 DefaultErrorDecoder 判断）
                    return Response.builder()
                            .request(request)
                            .status(SpecialHttpStatus.NOT_RETRYABLE_IO_EXCEPTION.getValue())
                            .reason(cause.getLocalizedMessage())
                            .requestTemplate(request.requestTemplate()).build();
                } else {
                    return Response.builder()
                            .request(request)
                            .status(SpecialHttpStatus.RETRYABLE_IO_EXCEPTION.getValue())
                            .reason(cause.getLocalizedMessage())
                            .requestTemplate(request.requestTemplate()).build();
                }
            }
            throw e;
        }
    }

    private String getServiceInstanceId(Request request) throws MalformedURLException {
        URL url = new URL(request.url());
        return Resilience4jUtil.getServiceInstance(url);
    }

    private String getServiceInstanceMethodId(Request request) throws MalformedURLException {
        URL url = new URL(request.url());
        //通过微服务名称 + 实例 + 方法的方式，获取唯一id
        return Resilience4jUtil.getServiceInstanceMethodId(url, request.requestTemplate().methodMetadata().method());
    }
}
