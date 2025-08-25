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
import io.github.opensabe.spring.cloud.parent.gateway.config.GatewayLoadBalanceByUidProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * 通过 uid 负载均衡调用
 */
@Component
@EnableConfigurationProperties(GatewayLoadBalanceByUidProperties.class)
public class LoadBalanceByUidFilter extends AbstractTracedFilter {

    @Autowired
    private GatewayLoadBalanceByUidProperties gatewayLoadBalanceByUidProperties;
    private final Cache<String, Boolean> filterCache = Caffeine.newBuilder()
            .maximumSize(10240).expireAfterAccess(Duration.ofHours(1)).build();

    @Override
    public Mono<Void> traced(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        if (isHitPathPatterns(filterCache, exchange, gatewayLoadBalanceByUidProperties.getPathPatterns())) {
            List<String> strings = headers.get("uid");
            if (CollectionUtils.isNotEmpty(strings)) {
                String uid = strings.get(0);
                if (StringUtils.isNotBlank(uid)) {
                    exchange.getAttributes().put(TracedCircuitBreakerRoundRobinLoadBalancer.LOAD_BALANCE_KEY, uid);
                }
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int ordered() {
        //顺序需要在负载均衡之前
        return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER - 1;
    }

    @Override
    protected boolean extraCondition(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();
        return Objects.equals(HttpMethod.GET, method);
    }
}
