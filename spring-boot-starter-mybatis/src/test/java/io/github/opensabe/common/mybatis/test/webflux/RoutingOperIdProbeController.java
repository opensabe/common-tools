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
package io.github.opensabe.common.mybatis.test.webflux;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.opensabe.common.mybatis.interceptor.WebfluxDataSourceSwitchInterceptor;
import io.github.opensabe.common.mybatis.webflux.WebFluxRoutingContext;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 在已执行 {@code chain.filter(exch)} 且经 {@code WebFilter} 中
 * {@code contextWrite(operId)} 与 {@code transformDeferredContextual+doOnEach} 的链上，
 * 用 {@code Mono.deferContextual} 取到与请求一致的 Reactor {@code ContextView}；
 * 再用 {@code subscribeOn} 在目标工作线程的同一个 {@code fromCallable} 里先
 * {@link WebFluxRoutingContext#restoreContextView} 再调用
 * {@link WebfluxDataSourceSwitchInterceptor#resolutionProbeOperIdFromBoundContext()}，
 * 避免在调用线程上写入 ThreadLocal、却在另一线程上读回（与 MyBatis 阻塞/JDBC 线程上解析 operId 的形式一致）。
 */
@RestController
@RequestMapping("/internal/probe")
public class RoutingOperIdProbeController {

    @GetMapping(value = "/oper/bounded-elastic", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> readOperIdOnBoundedElastic() {
        return Mono.deferContextual(
                cv -> Mono.fromCallable(() -> {
                    WebFluxRoutingContext.restoreContextView(cv);
                    return WebfluxDataSourceSwitchInterceptor.resolutionProbeOperIdFromBoundContext();
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    @GetMapping(value = "/oper/immediate", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> readOperIdOnImmediate() {
        return Mono.deferContextual(
                cv -> Mono.fromCallable(() -> {
                    WebFluxRoutingContext.restoreContextView(cv);
                    return WebfluxDataSourceSwitchInterceptor.resolutionProbeOperIdFromBoundContext();
                }).subscribeOn(Schedulers.immediate()));
    }
}
