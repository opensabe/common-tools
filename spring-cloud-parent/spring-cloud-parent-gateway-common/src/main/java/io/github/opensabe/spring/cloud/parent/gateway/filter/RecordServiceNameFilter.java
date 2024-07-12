package io.github.opensabe.spring.cloud.parent.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 记录本次请求调用微服务名称
 */
@Component
public class RecordServiceNameFilter extends AbstractTracedFilter {
    public static final String SERVICE_NAME = "SERVICE-NAME";

    @Override
    public Mono<Void> traced(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String host = route.getUri().getHost();
        return chain.filter(exchange.mutate().request(new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(exchange.getRequest().getHeaders());
                httpHeaders.set(SERVICE_NAME, host);
                return httpHeaders;
            }
        }).build());
    }

    @Override
    public int ordered() {
        return TraceIdFilter.ORDER + 1;
    }
}
