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
package io.github.opensabe.common.location.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.opensabe.common.location.vo.GeoLocationData;
import io.github.opensabe.common.location.vo.IpLocation;
import io.github.opensabe.common.testcontainers.integration.SingleValkeyIntegrationTest;
import io.github.opensabe.common.utils.json.JsonUtil;

/**
 * 位置服务集成测试（Valkey Testcontainers）。
 */
@ExtendWith({SpringExtension.class, SingleValkeyIntegrationTest.class})
@SpringBootTest(properties = {
        "eureka.client.enabled=false"
})
@DisplayName("位置服务集成测试（Valkey）")
public class LocationImplWithValkeyTest {

    private static final String TEST_IP = "203.0.113.10";

    @Autowired
    private IpToLocation ipToLocation;
    @Autowired
    private GeoLocation geoLocation;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleValkeyIntegrationTest.setProperties(registry);
    }

    /**
     * 预置 IP 缓存后，重复查询应返回一致的国家信息。
     */
    @Test
    @DisplayName("IP定位缓存命中后结果一致")
    public void testGetRegion() {
        // Seed cache so the test does not depend on flaky external IP geo APIs.
        IpLocation seeded = IpLocation.builder()
                .ip(TEST_IP)
                .city("Mountain View")
                .region("California")
                .country("United States")
                .latitude(37.42301)
                .longitude(-122.083352)
                .build();
        stringRedisTemplate.opsForValue().set("ip_location:" + TEST_IP, JsonUtil.toJSONString(seeded));

        GeoLocationData first = ipToLocation.getNearest(TEST_IP);
        GeoLocationData second = ipToLocation.getNearest(TEST_IP);
        Assertions.assertNotNull(first);
        Assertions.assertEquals(first, second);
        Assertions.assertEquals("United States", first.getCountry());
    }

    /**
     * 按经纬度查询最近地理位置应返回非空结果。
     */
    @Test
    @DisplayName("经纬度查询最近地理位置")
    public void testGetNearest() {
        GeoLocationData nearest = geoLocation.getNearest(37.42301, -122.083352);
        Assertions.assertNotNull(nearest);
        Assertions.assertFalse(nearest.isEmpty());
    }

    @EnableAutoConfiguration
    @Configuration
    public static class App {
    }
}
