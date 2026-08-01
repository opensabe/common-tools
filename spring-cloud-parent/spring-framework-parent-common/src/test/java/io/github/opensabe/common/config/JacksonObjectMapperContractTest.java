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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.module.blackbird.BlackbirdModule;

import io.github.opensabe.base.vo.IntValueEnum;
import io.github.opensabe.common.jackson.TimestampModule;
import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Upgrade gate: Spring ObjectMapper (TimestampModule + Blackbird) must stay aligned with JsonUtil
 * for LocalDateTime epoch-ms serialization after Boot 4 / Jackson 3 migration.
 */
@SpringBootTest(classes = JacksonObjectMapperContractTest.App.class)
@DisplayName("Jackson ObjectMapper ↔ JsonUtil 升级契约")
class JacksonObjectMapperContractTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void useSpringObjectMapper() {
        // Mirror SpringCommonUtilConfiguration: JsonUtil adopts the Spring ObjectMapper bean
        new JsonUtil(objectMapper);
    }

    @Test
    @DisplayName("TimestampModule 与 BlackbirdModule 已注册为 Spring Module beans")
    void modulesRegisteredAsBeans() {
        Map<String, JacksonModule> modules = applicationContext.getBeansOfType(JacksonModule.class);
        assertTrue(modules.values().stream().anyMatch(TimestampModule.class::isInstance));
        assertTrue(modules.values().stream().anyMatch(BlackbirdModule.class::isInstance));
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
