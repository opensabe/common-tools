package io.github.opensabe.common.internalcall;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ipv4CidrSetTest {

    @Test
    @DisplayName("CIDR 命中与未命中")
    void hitMiss() {
        Ipv4CidrSet set = Ipv4CidrSet.parse(List.of("10.0.0.0/8", "192.168.1.0/24"));
        assertTrue(set.contains("10.1.2.3"));
        assertTrue(set.contains("192.168.1.10"));
        assertFalse(set.contains("172.16.0.1"));
        assertFalse(set.contains("8.8.8.8"));
    }

    @Test
    @DisplayName("前导零 CIDR 启动失败")
    void leadingZeroRejected() {
        assertThrows(IllegalArgumentException.class, () -> Ipv4CidrSet.parse(List.of("010.0.0.0/8")));
    }

    @Test
    @DisplayName("非法 CIDR 启动失败")
    void illegalCidr() {
        assertThrows(IllegalArgumentException.class, () -> Ipv4CidrSet.parse(List.of("10.0.0.0")));
        assertThrows(IllegalArgumentException.class, () -> Ipv4CidrSet.parse(List.of("10.0.0.0/33")));
        assertThrows(IllegalArgumentException.class, () -> Ipv4CidrSet.parse(List.of("::1/128")));
    }

    @Test
    @DisplayName("空列表合法且不命中")
    void emptyList() {
        Ipv4CidrSet set = Ipv4CidrSet.parse(List.of());
        assertTrue(set.isEmpty());
        assertFalse(set.contains("10.0.0.1"));
    }

    @Test
    @DisplayName("IPv4-mapped 字符串分类为 UNSUPPORTED")
    void ipv4MappedClassifiedAsIpv6() {
        assertEquals(Ipv4CidrSet.PeerKind.IPV6_OR_MAPPED, Ipv4CidrSet.classifyPeer("::ffff:10.0.0.1"));
        Ipv4CidrSet set = Ipv4CidrSet.parse(List.of("10.0.0.0/8"));
        assertFalse(set.contains("::ffff:10.0.0.1"));
    }

    @Test
    @DisplayName("/0 匹配全部 IPv4")
    void slashZero() {
        Ipv4CidrSet set = Ipv4CidrSet.parse(List.of("0.0.0.0/0"));
        assertTrue(set.contains("1.2.3.4"));
        assertTrue(set.contains("255.255.255.255"));
    }
}
