package io.github.opensabe.common.utils;

import io.github.opensabe.common.utils.IpUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

public class IpUtilTest {

    @Test
    public void testGetIpFromHeader_FilterIp() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "0.0.0.0, 141.101.76.221, 0.0.0.0, 10.238.5.162");
        Assertions.assertEquals("141.101.76.221", IpUtil.getIpFromHeader(request));
    }

    @Test
    public void testGetIpFromHeader_FilterIp_SingleItem() {
        var request = new MockHttpServletRequest();
        request.addHeader("x-forwarded-for", "0.0.0.0");
        Assertions.assertEquals("0.0.0.0", IpUtil.getIpFromHeader(request));
    }

    @Test
    public void testGetIpFromHeader_FilterIp_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of("0.0.0.0, 141.101.76.221, 0.0.0.0, 10.238.5.162"));
        Assertions.assertEquals("141.101.76.221", IpUtil.getIpFromHeader(headers));
    }

    @Test
    public void testGetIpFromHeader_FilterIp_SingleItem_ByHeader() {
        var headers = new HttpHeaders();
        headers.put("x-forwarded-for", List.of("0.0.0.0"));
        Assertions.assertEquals("", IpUtil.getIpFromHeader(headers));
    }
}
