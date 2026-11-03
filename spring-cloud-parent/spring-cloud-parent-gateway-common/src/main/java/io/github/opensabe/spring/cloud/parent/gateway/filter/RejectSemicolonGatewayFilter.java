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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.github.opensabe.common.http.SemicolonPathGuard;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * Gateway early reject for URI paths containing {@code ;}.
 * Ordered right after {@link TraceIdFilter}.
 */
@Log4j2
@Component
@ConditionalOnProperty(
        prefix = "opensabe.http.reject-semicolon",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class RejectSemicolonGatewayFilter implements GlobalFilter, Ordered {

    public static final int ORDER = TraceIdFilter.ORDER + 1;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI uri = exchange.getRequest().getURI();
        String rawPath = uri.getRawPath();
        if (rawPath == null) {
            rawPath = uri.getPath();
        }
        if (SemicolonPathGuard.containsSemicolonInPath(rawPath)) {
            log.warn("RejectSemicolonGatewayFilter: reject path with semicolon path={}", rawPath);
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
