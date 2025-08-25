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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.parent.common.loadbalancer.TracedCircuitBreakerRoundRobinLoadBalancer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * 在所有的 GlobalFilter 开头
 * 在响应中加入当前 trace 的 traceId
 */
@Log4j2
@Component
public class TraceIdFilter implements GlobalFilter, Ordered {
    public static final int ORDER = HIGHEST_PRECEDENCE;

    public static final String TRACE_ID = "traceId";

    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    public static Observation getObservation(ServerWebExchange exchange) {
        return exchange.getAttribute(TracedCircuitBreakerRoundRobinLoadBalancer.OBSERVATION_KEY);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ;
        return Mono.deferContextual(contextView -> {
            //获取当前的 observation 是参考代码：ServerHttpObservationFilter 的 doFilterInternal 方法
            //在上面的代码是 context.put(ObservationThreadLocalAccessor.KEY, observation)
            //获取当前的 observation，或者创建一个新的 observation
            Observation observation = null;
            if (contextView.hasKey(ObservationThreadLocalAccessor.KEY)) {
                observation = contextView.get(ObservationThreadLocalAccessor.KEY);
            } else {
                observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
            }
            exchange.getAttributes().put(TracedCircuitBreakerRoundRobinLoadBalancer.OBSERVATION_KEY, observation);
            exchange.getResponse().getHeaders().add(TRACE_ID, UnifiedObservationFactory.getTraceContext(observation).traceId());
            return chain.filter(exchange);
        });
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
