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
