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

package io.github.opensabe.common.internalcall;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalCallPathMatcherTest {

    @Test
    @DisplayName("默认 actuator 排除")
    void actuator() {
        InternalCallPathMatcher matcher = new InternalCallPathMatcher(List.of("/actuator/**", "/health"));
        assertTrue(matcher.isExcluded("/actuator/health", null));
        assertTrue(matcher.isExcluded("/health", ""));
        assertFalse(matcher.isExcluded("/api/order", null));
    }

    @Test
    @DisplayName("分号路径参数不能绕过排除或误匹配")
    void semicolonBypass() {
        InternalCallPathMatcher matcher = new InternalCallPathMatcher(List.of("/excluded/**"));
        // /excluded/..;/admin → strip semicolon → /excluded/../admin → collapse → /admin
        assertFalse(matcher.isExcluded("/excluded/..;/admin", null));
        assertTrue(matcher.isExcluded("/excluded/foo;jsessionid=1", null));
    }

    @Test
    @DisplayName("context-path 边界剥离")
    void contextPath() {
        InternalCallPathMatcher matcher = new InternalCallPathMatcher(List.of("/health"));
        assertTrue(matcher.isExcluded("/app/health", "/app"));
        assertFalse(matcher.isExcluded("/app2/health", "/app"));
    }

    @Test
    @DisplayName("编码斜杠拒绝，不当作排除命中")
    void encodedSlash() {
        InternalCallPathMatcher matcher = new InternalCallPathMatcher(List.of("/excluded/**"));
        assertFalse(matcher.isExcluded("/excluded%2fadmin", null));
    }

    @Test
    @DisplayName("编码分号 %3b 解码后再剥离，不能绕过排除边界")
    void encodedSemicolon() {
        InternalCallPathMatcher matcher = new InternalCallPathMatcher(List.of("/excluded/**"));
        // /excluded/..%3b/admin → decode → /excluded/..;/admin → strip ; → /excluded/../admin → /admin
        assertFalse(matcher.isExcluded("/excluded/..%3b/admin", null));
    }

    @Test
    @DisplayName("%2e%2e 折叠出排除前缀")
    void encodedDotDot() {
        InternalCallPathMatcher matcher = new InternalCallPathMatcher(List.of("/actuator/**"));
        assertFalse(matcher.isExcluded("/actuator/%2e%2e/x", null));
    }
}
