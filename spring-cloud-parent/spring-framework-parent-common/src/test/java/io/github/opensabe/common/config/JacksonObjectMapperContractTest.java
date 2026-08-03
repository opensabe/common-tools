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
package io.github.opensabe.common.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.module.blackbird.BlackbirdModule;

import io.github.opensabe.base.vo.IntValueEnum;
import io.github.opensabe.common.jackson.TimestampModule;
import io.github.opensabe.common.utils.SpringUtil;
import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Spring Jackson / JsonUtil / SpringUtil 升级契约：验证自动配置桥接在无手动劫持下生效。
 */
@SpringBootTest(classes = JacksonObjectMapperContractTest.App.class)
@DisplayName("Jackson ObjectMapper ↔ JsonUtil 升级契约")
class JacksonObjectMapperContractTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JsonUtilSpringBridge jsonUtilSpringBridge;

    @Test
    @DisplayName("JsonUtilSpringBridge 已将 JsonUtil 静态 mapper 桥接到 Boot JsonMapper（同一实例）")
    void jsonUtilAdoptedSpringJsonMapperWithoutManualHijack() {
        assertNotNull(jsonUtilSpringBridge);
        assertSame(jsonMapper, objectMapper, "Boot JsonMapper must be the ObjectMapper bean");
        assertSame(jsonMapper, JsonUtil.mapper(), "JsonUtil must adopt the Spring JsonMapper via bridge");
    }

    @Test
    @DisplayName("SpringUtil 已持有 ApplicationContext")
    void springUtilHoldsApplicationContext() {
        assertNotNull(SpringUtil.getApplicationContext());
        assertSame(applicationContext, SpringUtil.getApplicationContext());
    }

    @Test
    @DisplayName("TimestampModule 与 BlackbirdModule 已注册为 Spring Module beans")
    void modulesRegisteredAsBeans() {
        Map<String, JacksonModule> modules = applicationContext.getBeansOfType(JacksonModule.class);
        assertTrue(modules.values().stream().anyMatch(TimestampModule.class::isInstance));
        assertTrue(modules.values().stream().anyMatch(BlackbirdModule.class::isInstance));
        assertTrue(modules.containsKey("timestampModule"), "bean name should be timestampModule after typo fix");
    }

    @Test
    @DisplayName("Boot JsonMapper 已启用 WRITE_DATES_AS_TIMESTAMPS")
    void writeDatesAsTimestampsEnabled() {
        assertTrue(
                jsonMapper.isEnabled(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS),
                "JsonMapperBuilderCustomizer must enable WRITE_DATES_AS_TIMESTAMPS");
    }

    @Test
    @DisplayName("LocalDateTime 序列化为 epoch 毫秒数字而非 ISO 字符串")
    void localDateTimeSerializedAsEpochMillis() throws Exception {
        LocalDateTime time = LocalDateTime.of(2024, 6, 15, 12, 30, 45, 123_000_000)
                .truncatedTo(ChronoUnit.MILLIS);
        Payload payload = new Payload("n1", time, SampleEnum.ONE, new NestedRecord("inner"));

        String viaBean = objectMapper.writeValueAsString(payload);
        String viaJsonUtil = JsonUtil.toJSONString(payload);

        JsonNode beanNode = objectMapper.readTree(viaBean);
        JsonNode utilNode = objectMapper.readTree(viaJsonUtil);

        assertTrue(beanNode.get("time").isNumber(), "Spring ObjectMapper must emit numeric timestamp");
        assertTrue(utilNode.get("time").isNumber(), "JsonUtil must emit numeric timestamp");
        assertFalse(beanNode.get("time").asText().contains("T"));

        long expected = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        assertEquals(expected, beanNode.get("time").asLong());
        assertEquals(expected, utilNode.get("time").asLong());
        assertEquals(beanNode, utilNode);
    }

    @Test
    @DisplayName("ObjectMapper 与 JsonUtil 对同一对象序列化结果等价并可反序列化")
    void springMapperAndJsonUtilRoundTripParity() throws Exception {
        LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        Payload payload = new Payload("n1", time, SampleEnum.TWO, new NestedRecord("x"));

        String viaBean = objectMapper.writeValueAsString(payload);
        Payload fromBean = objectMapper.readValue(viaBean, Payload.class);
        Payload fromUtil = JsonUtil.parseObject(viaBean, Payload.class);

        assertEquals(payload.getName(), fromBean.getName());
        assertEquals(payload.getTime(), fromBean.getTime());
        assertEquals(payload.getType(), fromBean.getType());
        assertEquals(payload.getNested(), fromBean.getNested());
        assertEquals(fromBean.getName(), fromUtil.getName());
        assertEquals(fromBean.getTime(), fromUtil.getTime());
    }

    @SpringBootApplication
    static class App {
    }

    enum SampleEnum implements IntValueEnum {
        ONE(1), TWO(2);

        private final int value;

        SampleEnum(int value) {
            this.value = value;
        }

        @Override
        public Integer getValue() {
            return value;
        }
    }

    record NestedRecord(String value) {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Payload {
        private String name;
        private LocalDateTime time;
        private SampleEnum type;
        private NestedRecord nested;
    }
}
