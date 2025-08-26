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
package io.github.opensabe.common.utils;

import java.net.InetAddress;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

@DisplayName("IP地址工具类测试")
public class IpUtilTest {

    // ==================== getIpFromHeader(HttpServletRequest) 方法测试 ====================

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 过滤无效IP地址")
    public void testGetIpFromHeader_FilterIp() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "0.0.0.0, 141.101.76.221, 0.0.0.0, 10.238.5.162");
        Assertions.assertEquals("141.101.76.221", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 单个IP地址")
    public void testGetIpFromHeader_FilterIp_SingleItem() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "0.0.0.0");
        Assertions.assertEquals("0.0.0.0", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 多个代理IP，第一个是公网IP")
    public void testGetIpFromHeader_MultipleProxies_FirstPublic() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "203.208.60.1, 10.0.0.1, 192.168.1.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 多个代理IP，第一个是内网IP，最后是公网IP")
    public void testGetIpFromHeader_MultipleProxies_LastPublic() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "10.0.0.1, 192.168.1.1, 203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 所有都是内网IP")
    public void testGetIpFromHeader_AllInternalIps() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "10.0.0.1, 192.168.1.1, 172.16.0.1");
        Assertions.assertEquals("10.0.0.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 使用Proxy-Client-IP头")
    public void testGetIpFromHeader_ProxyClientIP() {
        var request = new MockHttpServletRequest();
        request.addHeader("Proxy-Client-IP", "203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 使用WL-Proxy-Client-IP头")
    public void testGetIpFromHeader_WLProxyClientIP() {
        var request = new MockHttpServletRequest();
        request.addHeader("WL-Proxy-Client-IP", "203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 使用HTTP_CLIENT_IP头")
    public void testGetIpFromHeader_HTTPClientIP() {
        var request = new MockHttpServletRequest();
        request.addHeader("HTTP_CLIENT_IP", "203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 使用HTTP_X_FORWARDED_FOR头")
    public void testGetIpFromHeader_HTTPXForwardedFor() {
        var request = new MockHttpServletRequest();
        request.addHeader("HTTP_X_FORWARDED_FOR", "203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 使用X-Real-IP头")
    public void testGetIpFromHeader_XRealIP() {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 使用RemoteAddr")
    public void testGetIpFromHeader_RemoteAddr() {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 优先级测试：x-forwarded-for优先")
    public void testGetIpFromHeader_PriorityOrder() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "203.208.60.1");
        request.addHeader("Proxy-Client-IP", "203.208.60.2");
        request.addHeader("X-Real-IP", "203.208.60.3");
        request.setRemoteAddr("203.208.60.4");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 空值处理")
    public void testGetIpFromHeader_EmptyValues() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "");
        request.addHeader("Proxy-Client-IP", "   ");
        request.addHeader("WL-Proxy-Client-IP", "unknown");
        request.addHeader("HTTP_CLIENT_IP", "UNKNOWN");
        request.addHeader("HTTP_X_FORWARDED_FOR", "Unknown");
        request.addHeader("X-Real-IP", "unknown");
        request.setRemoteAddr("203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    // ==================== getIpFromHeader(HttpHeaders) 方法测试 ====================

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 过滤无效IP地址")
    public void testGetIpFromHeader_FilterIp_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of("0.0.0.0, 141.101.76.221, 0.0.0.0, 10.238.5.162"));
        Assertions.assertEquals("141.101.76.221", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 单个无效IP地址")
    public void testGetIpFromHeader_FilterIp_SingleItem_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of("0.0.0.0"));
        Assertions.assertEquals("", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 使用Proxy-Client-IP头")
    public void testGetIpFromHeader_ProxyClientIP_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("Proxy-Client-IP", List.of("203.208.60.1"));
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 使用WL-Proxy-Client-IP头")
    public void testGetIpFromHeader_WLProxyClientIP_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("WL-Proxy-Client-IP", List.of("203.208.60.1"));
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 使用HTTP_CLIENT_IP头")
    public void testGetIpFromHeader_HTTPClientIP_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("HTTP_CLIENT_IP", List.of("203.208.60.1"));
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 使用HTTP_X_FORWARDED_FOR头")
    public void testGetIpFromHeader_HTTPXForwardedFor_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("HTTP_X_FORWARDED_FOR", List.of("203.208.60.1"));
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 使用X-Real-IP头")
    public void testGetIpFromHeader_XRealIP_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("X-Real-IP", List.of("203.208.60.1"));
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 优先级测试")
    public void testGetIpFromHeader_PriorityOrder_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of("203.208.60.1"));
        headers.put("Proxy-Client-IP", List.of("203.208.60.2"));
        headers.put("X-Real-IP", List.of("203.208.60.3"));
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 空值处理")
    public void testGetIpFromHeader_EmptyValues_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of(""));
        headers.put("Proxy-Client-IP", List.of("   "));
        headers.put("WL-Proxy-Client-IP", List.of("unknown"));
        headers.put("HTTP_CLIENT_IP", List.of("UNKNOWN"));
        headers.put("HTTP_X_FORWARDED_FOR", List.of("Unknown"));
        headers.put("X-Real-IP", List.of("unknown"));
        Assertions.assertEquals("", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 空列表处理")
    public void testGetIpFromHeader_EmptyList_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of());
        Assertions.assertEquals("", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 空字符串列表处理")
    public void testGetIpFromHeader_EmptyStringList_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of(""));
        Assertions.assertEquals("", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 复杂IP字符串处理")
    public void testGetIpFromHeader_ComplexIpString_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of("0.0.0.0, 10.0.0.1, 203.208.60.1, 192.168.1.1, 0.0.0.0"));
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(headers));
    }

    // ==================== findFirstNonLoopbackAddress 方法测试 ====================

    @Test
    @DisplayName("测试查找第一个非回环地址 - 基本功能")
    public void testFindFirstNonLoopbackAddress() {
        InetAddress result = IpUtil.findFirstNonLoopbackAddress();
        // 由于网络环境不同，结果可能为null或有效地址
        if (result != null) {
            Assertions.assertFalse(result.isLoopbackAddress());
            Assertions.assertTrue(result instanceof java.net.Inet4Address);
        }
    }

    // ==================== 边界场景和复杂场景测试 ====================

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 边界场景：单个IP")
    public void testGetIpFromHeader_Boundary_SingleIP() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 边界场景：空字符串")
    public void testGetIpFromHeader_Boundary_EmptyString() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "");
        request.setRemoteAddr("203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 边界场景：只有空格")
    public void testGetIpFromHeader_Boundary_OnlySpaces() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "   ");
        request.setRemoteAddr("203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 边界场景：unknown值")
    public void testGetIpFromHeader_Boundary_UnknownValue() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "unknown");
        request.setRemoteAddr("203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 边界场景：UNKNOWN值")
    public void testGetIpFromHeader_Boundary_UNKNOWNValue() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "UNKNOWN");
        request.setRemoteAddr("203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 边界场景：Unknown值")
    public void testGetIpFromHeader_Boundary_UnknownMixedCase() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "Unknown");
        request.setRemoteAddr("203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 复杂场景：多个代理，混合内网外网")
    public void testGetIpFromHeader_Complex_MultipleProxiesMixed() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "0.0.0.0, 10.0.0.1, 203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 复杂场景：所有头都为空，使用RemoteAddr")
    public void testGetIpFromHeader_Complex_AllHeadersEmpty() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "");
        request.addHeader("Proxy-Client-IP", "");
        request.addHeader("WL-Proxy-Client-IP", "");
        request.addHeader("HTTP_CLIENT_IP", "");
        request.addHeader("HTTP_X_FORWARDED_FOR", "");
        request.addHeader("X-Real-IP", "");
        request.setRemoteAddr("203.208.60.1");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从请求头中获取IP地址 - 复杂场景：IP字符串包含空格")
    public void testGetIpFromHeader_Complex_IpStringWithSpaces() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", " 203.208.60.1 , 10.0.0.1 , 192.168.1.1 ");
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(request));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 边界场景：空列表")
    public void testGetIpFromHeader_Boundary_EmptyList_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of());
        Assertions.assertEquals("", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 边界场景：空字符串列表")
    public void testGetIpFromHeader_Boundary_EmptyStringList_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of(""));
        Assertions.assertEquals("", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 边界场景：null列表")
    public void testGetIpFromHeader_Boundary_NullList_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", null);
        Assertions.assertEquals("", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 复杂场景：IP字符串为空")
    public void testGetIpFromHeader_Complex_EmptyIpString_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of(""));
        Assertions.assertEquals("", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 复杂场景：IP字符串只有0.0.0.0")
    public void testGetIpFromHeader_Complex_OnlyZeroIp_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of("0.0.0.0"));
        Assertions.assertEquals("", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 复杂场景：IP字符串包含0.0.0.0和有效IP")
    public void testGetIpFromHeader_Complex_ZeroIpWithValidIp_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of("0.0.0.0, 203.208.60.1"));
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 复杂场景：IP字符串包含多个0.0.0.0")
    public void testGetIpFromHeader_Complex_MultipleZeroIps_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of("0.0.0.0, 0.0.0.0, 0.0.0.0"));
        Assertions.assertEquals("", IpUtil.getIpFromHeader(headers));
    }

    @Test
    @DisplayName("测试从HttpHeaders中获取IP地址 - 复杂场景：IP字符串包含空格和0.0.0.0")
    public void testGetIpFromHeader_Complex_SpacesWithZeroIp_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of(" 0.0.0.0 , 203.208.60.1 , 0.0.0.0 "));
        Assertions.assertEquals("203.208.60.1", IpUtil.getIpFromHeader(headers));
    }
}
