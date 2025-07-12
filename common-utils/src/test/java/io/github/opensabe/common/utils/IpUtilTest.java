package io.github.opensabe.common.utils;

import io.github.opensabe.common.utils.IpUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

@DisplayName("IP地址工具类测试")
public class IpUtilTest {

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
}
