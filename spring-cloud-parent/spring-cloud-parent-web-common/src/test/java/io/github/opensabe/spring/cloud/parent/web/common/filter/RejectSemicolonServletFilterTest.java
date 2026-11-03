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
package io.github.opensabe.spring.cloud.parent.web.common.filter;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RejectSemicolonServletFilter")
class RejectSemicolonServletFilterTest {

    @Test
    @DisplayName("path with semicolon returns 400")
    void rejectsSemicolonPath() throws Exception {
        RejectSemicolonServletFilter filter = new RejectSemicolonServletFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api;x=/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean continued = new AtomicBoolean(false);
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(
                    jakarta.servlet.ServletRequest request,
                    jakarta.servlet.ServletResponse response
            ) {
                continued.set(true);
            }
        };

        filter.doFilter(request, response, chain);

        assertEquals(400, response.getStatus());
        assertFalse(continued.get());
    }

    @Test
    @DisplayName("semicolon only in query is allowed")
    void allowsQuerySemicolon() throws Exception {
        RejectSemicolonServletFilter filter = new RejectSemicolonServletFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api");
        request.setQueryString("a=1;2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean continued = new AtomicBoolean(false);
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(
                    jakarta.servlet.ServletRequest request,
                    jakarta.servlet.ServletResponse response
            ) {
                continued.set(true);
            }
        };

        filter.doFilter(request, response, chain);

        assertTrue(continued.get());
        assertEquals(200, response.getStatus());
    }
}
