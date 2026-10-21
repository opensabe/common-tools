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

import io.github.opensabe.common.internalcall.CallerIdentityProvider;
import io.github.opensabe.common.internalcall.InternalCallPathMatcher;
import io.github.opensabe.common.internalcall.InternalCallProperties;
import io.github.opensabe.common.internalcall.InternalCallSigner;
import io.github.opensabe.common.internalcall.InternalHostMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InternalCallOutboundGatewayFilterTest {

    @Test
    @DisplayName("出站 order 严格小于 MAX-1")
    void orderBeforeWebsocket() {
        assertTrue(InternalCallOutboundGatewayFilter.ORDER < Integer.MAX_VALUE - 1);
        assertEquals(Integer.MAX_VALUE - 2, InternalCallOutboundGatewayFilter.ORDER);
    }

    @Test
    @DisplayName("同一原始 exchange 进入两次得到两个 nonce；公网只剥头")
    void resignOnRetryAndStripPublic() {
        InternalCallOutboundGatewayFilter filter = filter("secret");

        List<String> nonces = new ArrayList<>();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://gw/api")
                        .header(props().getHeaders().getSign(), "forged")
                        .header(props().getHeaders().getNonce(), "old")
                        .build()
        );
        // Post-LB URL is instance http; lb detected via scheme prefix
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, URI.create("http://10.0.1.5:8080"));
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR, "lb");

        GatewayFilterChain capture = e -> {
            nonces.add(e.getRequest().getHeaders().getFirst(props().getHeaders().getNonce()));
            return Mono.empty();
        };
        filter.traced(exchange, capture).block();
        filter.traced(exchange, capture).block();
        assertEquals(2, nonces.size());
        assertNotEquals(nonces.get(0), nonces.get(1));

        MockServerWebExchange publicEx = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://gw/api")
                        .header(props().getHeaders().getSign(), "forged")
                        .build()
        );
        publicEx.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, URI.create("https://api.example.com/x"));
        AtomicReference<String> sign = new AtomicReference<>("unset");
        filter.traced(publicEx, e -> {
            sign.set(e.getRequest().getHeaders().getFirst(props().getHeaders().getSign()));
            return Mono.empty();
        }).block();
        assertNull(sign.get());
    }

    @Test
    @DisplayName("route scheme=lb 时即使 REQUEST_URL 是公网实例 host 仍加签")
    void lbRouteWithPublicLookingInstanceStillSigns() {
        InternalCallOutboundGatewayFilter filter = filter("secret");
        Route route = mock(Route.class);
        when(route.getUri()).thenReturn(URI.create("lb://order-service"));

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://gw/api").build());
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, URI.create("http://unusual.example.com:8080"));
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);

        AtomicReference<String> sign = new AtomicReference<>();
        filter.traced(exchange, e -> {
            sign.set(e.getRequest().getHeaders().getFirst(props().getHeaders().getSign()));
            return Mono.empty();
        }).block();
        assertNotNull(sign.get());
    }

    @Test
    @DisplayName("forward 路由只剥头不加签")
    void forwardStripOnly() {
        InternalCallOutboundGatewayFilter filter = filter("secret");
        Route route = mock(Route.class);
        when(route.getUri()).thenReturn(URI.create("forward:///local"));

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://gw/api")
                        .header(props().getHeaders().getSign(), "forged")
                        .build()
        );
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, URI.create("forward:///local"));

        AtomicReference<String> sign = new AtomicReference<>("unset");
        filter.traced(exchange, e -> {
            sign.set(e.getRequest().getHeaders().getFirst(props().getHeaders().getSign()));
            return Mono.empty();
        }).block();
        assertNull(sign.get());
    }

    private static InternalCallProperties props() {
        InternalCallProperties props = new InternalCallProperties();
        props.setEnabled(true);
        props.setSharedSecret("secret");
        return props;
    }

    private static InternalCallOutboundGatewayFilter filter(String secret) {
        InternalCallProperties props = new InternalCallProperties();
        props.setEnabled(true);
        props.setSharedSecret(secret);
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1_700_000_000_000L), ZoneOffset.UTC);
        return new InternalCallOutboundGatewayFilter(
                props,
                new InternalCallSigner(props, clock),
                new CallerIdentityProvider(new MockEnvironment().withProperty("spring.application.name", "gw")),
                new InternalHostMatcher(List.of("*.svc.cluster.local")),
                new InternalCallPathMatcher(List.of())
        );
    }
}
