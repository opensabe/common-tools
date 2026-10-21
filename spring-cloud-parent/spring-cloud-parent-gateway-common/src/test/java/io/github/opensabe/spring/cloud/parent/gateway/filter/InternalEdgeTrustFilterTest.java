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

import io.github.opensabe.common.internalcall.InternalCallFailureHandler;
import io.github.opensabe.common.internalcall.InternalCallPathMatcher;
import io.github.opensabe.common.internalcall.InternalCallProperties;
import io.github.opensabe.common.internalcall.InternalCallSigner;
import io.github.opensabe.common.internalcall.InternalCallVerifier;
import io.github.opensabe.common.internalcall.Ipv4CidrSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalEdgeTrustFilterTest {

    @Test
    @DisplayName("可信 CIDR 放行；失败仍放行")
    void trustedAndUntrustedStillPass() {
        InternalCallProperties props = new InternalCallProperties();
        props.setEnabled(true);
        props.getEdge().setTrustedCidrs(List.of("10.0.0.0/8"));
        Clock clock = Clock.systemUTC();
        InternalEdgeTrustFilter filter = new InternalEdgeTrustFilter(
                props,
                Ipv4CidrSet.parse(props.getEdge().getTrustedCidrs()),
                new InternalCallVerifier(props, new InternalCallSigner(props, clock), clock),
                new InternalCallFailureHandler(props),
                new InternalCallPathMatcher(List.of())
        );

        AtomicBoolean passed = new AtomicBoolean(false);
        GatewayFilterChain chain = e -> {
            passed.set(true);
            return Mono.empty();
        };

        MockServerWebExchange ok = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://gw/api")
                        .remoteAddress(new InetSocketAddress("10.1.2.3", 12345))
                        .build()
        );
        filter.traced(ok, chain).block();
        assertTrue(passed.get());

        passed.set(false);
        MockServerWebExchange bad = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://gw/api")
                        .remoteAddress(new InetSocketAddress("8.8.8.8", 12345))
                        .build()
        );
        filter.traced(bad, chain).block();
        assertTrue(passed.get());
        assertEquals(TraceIdFilter.ORDER + 2, filter.ordered());
    }

    @Test
    @DisplayName("Mock 无 native request 时 peerHost 回退到 remoteAddress")
    void peerHostFallsBackWithoutNativeRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://gw/api")
                .remoteAddress(new InetSocketAddress("10.9.8.7", 443))
                .build();
        assertEquals("10.9.8.7", InternalEdgeTrustFilter.peerHost(request));
    }
}
