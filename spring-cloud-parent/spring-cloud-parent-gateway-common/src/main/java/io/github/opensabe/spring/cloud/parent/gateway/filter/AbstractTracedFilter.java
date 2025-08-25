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
package io.github.opensabe.spring.cloud.parent.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.observation.Observation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;


/**
 * 所有 filter 的子类
 * 主要是：
 * 1. filter 有时候不只是拼接 webflux 链路代码，而是直接对于请求可能有些操作，不涉及 io，这里不再链路的日志输出没有 trace 信息，只有链路中中的日志才会有链路信息。
 * 2. 涉及 io 操作，可能因为某些原因，webflux 的 context 中断，导致 context 中的 Observation 没了，导致后续日志没有链路信息
 * 所以，为了保险，我们在第一个 Filter，即 TraceIdFilter 中放入 Observation，后续的其他 Filter 取出来使用，保证我们自定义的 Filter 的日志都有需要的 traceId 和 spanId
 */
public abstract class AbstractTracedFilter implements GlobalFilter, Ordered {
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Observation observation = TraceIdFilter.getObservation(exchange);
        return observation.scoped(() -> traced(exchange, chain));
    }

    protected abstract Mono<Void> traced(ServerWebExchange exchange, GatewayFilterChain chain);

    @Override
    public int getOrder() {
        //验证顺序一定在 TraceIdFilter 之后
        //这是我们这里写死的规定，自定义的 GlobalFilter 不准在 TraceIdFilter 之前
        int order0 = ordered();
        if (order0 <= TraceIdFilter.ORDER) {
            throw new IllegalArgumentException("An AbstractTracedFilter should not have an order below the order of TraceIdFilter");
        }
        return order0;
    }

    protected abstract int ordered();

    public Boolean isHitPathPatterns(Cache<String, Boolean> filterCache, ServerWebExchange exchange, Set<String> pathPatterns) {
        ServerHttpRequest request = exchange.getRequest();
        RequestPath path = request.getPath();
        HttpMethod method = request.getMethod();

        return filterCache.get(method.name() + '-' + path.value(), k -> {
            if (extraCondition(exchange)) {
                return true;
            }
            if (CollectionUtils.isNotEmpty(pathPatterns)) {
                Optional<String> first = pathPatterns.stream().filter(pattern ->
                        antPathMatcher.match(pattern, path.value())
                ).findFirst();
                return first.isPresent();
            }
            return false;
        });
    }

    protected boolean extraCondition(ServerWebExchange exchange) {
        return false;
    }
}
