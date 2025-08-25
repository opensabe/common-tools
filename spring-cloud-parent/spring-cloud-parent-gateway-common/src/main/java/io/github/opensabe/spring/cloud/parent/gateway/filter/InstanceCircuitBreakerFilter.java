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

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * 实例级别的断路器
 */
@Log4j2
@Component
public class InstanceCircuitBreakerFilter extends AbstractTracedFilter {
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public Mono<Void> traced(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI url = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String routeId = route.getId();
        String instanceId = url.getHost() + ":" + url.getPort();
        CircuitBreaker circuitBreaker;
        try {
            circuitBreaker = circuitBreakerRegistry.circuitBreaker(instanceId, routeId);
        } catch (ConfigurationNotFoundException e) {
            circuitBreaker = circuitBreakerRegistry.circuitBreaker(instanceId);
        }
        log.info("send request to: {}: buffered calls: {}, successful: {}", url,
                circuitBreaker.getMetrics().getNumberOfBufferedCalls(),
                circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()
        );
        //这里使用默认的封装 CircuitBreakerOperator
        //内部调用是我们可控的，某个接口 4XX 或者 5XX 可能确实出了什么问题，但是外部调用不可控。
        //不像公共依赖中对于 WebClient 我们针对 4XX 和 5XX 的响应码也断路
        //恶意调用可能一直触发我们某个接口 4XX 或者 5XX，但是正常调用不会。
        //所以，这里我们使用默认的，不看响应码，只对于异常进行断路（例如 IOException，连接超时，读取超时等等）
        return chain.filter(exchange).transform(CircuitBreakerOperator.of(circuitBreaker));
    }

    @Override
    public int ordered() {
        try {
            //必须在负载均衡器之后，这样才能拿到最终的要调用的实例 ip port
            return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 1;
        } catch (Exception e) {
            return 10151;
        }
    }

}