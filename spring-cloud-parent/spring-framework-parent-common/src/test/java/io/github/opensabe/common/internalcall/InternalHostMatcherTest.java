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

class InternalHostMatcherTest {

    private final InternalHostMatcher matcher = new InternalHostMatcher(List.of("*.svc.cluster.local"));

    @Test
    @DisplayName("短服务名视为内网")
    void shortServiceName() {
        assertTrue(matcher.isInternalHost("order-service", null));
        assertTrue(matcher.isInternalHost("order-service", "order-service"));
    }

    @Test
    @DisplayName("cluster.local Ant 匹配")
    void clusterLocal() {
        assertTrue(matcher.isInternalHost("order.ns.svc.cluster.local", null));
    }

    @Test
    @DisplayName("RFC1918 与 loopback")
    void privateIp() {
        assertTrue(matcher.isInternalHost("10.1.2.3", null));
        assertTrue(matcher.isInternalHost("192.168.0.1", null));
        assertTrue(matcher.isInternalHost("127.0.0.1", null));
        assertFalse(matcher.isInternalHost("8.8.8.8", null));
    }

    @Test
    @DisplayName("公网域名不加签")
    void publicHost() {
        assertFalse(matcher.isInternalHost("api.example.com", null));
        assertFalse(matcher.isInternalHost("api.example.com", "api"));
    }

    @Test
    @DisplayName("serviceName 相等覆盖")
    void serviceNameEquals() {
        assertTrue(matcher.isInternalHost("api.example.com", "api.example.com"));
    }
}
