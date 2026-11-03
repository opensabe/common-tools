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
package io.github.opensabe.spring.cloud.parent.webflux.common.filter;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RejectSemicolonWebFilter")
class RejectSemicolonWebFilterTest {

    @Test
    @DisplayName("path with semicolon returns 400")
    void rejectsSemicolonPath() {
        RejectSemicolonWebFilter filter = new RejectSemicolonWebFilter();
        AtomicBoolean passed = new AtomicBoolean(false);
        WebFilterChain chain = e -> {
            passed.set(true);
            return Mono.empty();
        };

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://localhost/api;x=/").build()
        );
        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        assertFalse(passed.get());
    }

    @Test
    @DisplayName("semicolon only in query is allowed")
    void allowsQuerySemicolon() {
        RejectSemicolonWebFilter filter = new RejectSemicolonWebFilter();
        AtomicBoolean passed = new AtomicBoolean(false);
        WebFilterChain chain = e -> {
            passed.set(true);
            return Mono.empty();
        };

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://localhost/api?a=1;2").build()
        );
        filter.filter(exchange, chain).block();

        assertTrue(passed.get());
    }
}
