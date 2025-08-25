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
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.opensabe.spring.cloud.parent.common.loadbalancer.TracedCircuitBreakerRoundRobinLoadBalancer;
import io.github.opensabe.spring.cloud.parent.gateway.config.GatewayBatchLoadBalanceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 如果是批量请求，走RoundRobin负载均衡
 */
@Component
@EnableConfigurationProperties(GatewayBatchLoadBalanceProperties.class)
public class BatchLoadBalanceFilter extends AbstractTracedFilter{

    private final Cache<String, Boolean> filterCache = Caffeine.newBuilder()
            .maximumSize(10240).expireAfterAccess(Duration.ofHours(1)).build();
    private final Cache<String, AtomicInteger> roundRobinCache = Caffeine.newBuilder()
            .maximumSize(10240).expireAfterWrite(Duration.ofHours(1)).build();
    @Autowired
    private GatewayBatchLoadBalanceProperties gatewayBatchLoadBalanceProperties;
    @Override
    protected Mono<Void> traced(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        RequestPath path = request.getPath();
        if (isHitPathPatterns(filterCache, exchange, gatewayBatchLoadBalanceProperties.getPathPatterns())) {
            AtomicInteger count = roundRobinCache.get(path.value(), k -> new AtomicInteger(0));
            exchange.getAttributes().put(TracedCircuitBreakerRoundRobinLoadBalancer.ROUND_ROBIN_KEY, count.incrementAndGet());
        }
        return chain.filter(exchange);
    }

    @Override
    protected int ordered() {
        //顺序需要在负载均衡之前
        return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER - 1;
    }
}
