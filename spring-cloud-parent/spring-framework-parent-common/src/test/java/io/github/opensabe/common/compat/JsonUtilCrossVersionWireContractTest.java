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
package io.github.opensabe.common.compat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import tools.jackson.databind.JsonNode;

import io.github.opensabe.common.compat.jackson2.Jackson2TimestampModule;
import io.github.opensabe.common.utils.json.JsonUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JsonUtil ↔ Jackson2 线格式交叉（模拟 Redis STRING）")
class JsonUtilCrossVersionWireContractTest {

    private static ObjectMapper legacyJackson2() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES);
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jackson2TimestampModule());
        return mapper;
    }

    private static SamplePayload sample() {
        LocalDateTime updated = LocalDateTime.of(2024, 6, 15, 12, 30, 45, 123_000_000)
                .truncatedTo(ChronoUnit.MILLIS);
        Date created = Date.from(updated.minusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        return new SamplePayload(
                "id-001",
                updated,
                created,
                SamplePayload.PlainStatus.ACTIVE,
                2,
                null,
                new SamplePayload.Nested("ref-a", 1.5),
                List.of("x", "y"),
                Map.of("a", 1, "b", 2),
                new BigDecimal("1.25"),
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        );
    }

    @Test
    @DisplayName("old Jackson2 序列化 → JsonUtil 反序列化")
    void oldSerializeNewDeserialize() throws Exception {
        SamplePayload original = sample();
        String blob = legacyJackson2().writeValueAsString(original);

        JsonNode node = JsonUtil.mapper().readTree(blob);
        assertTrue(node.get("updatedAt").isNumber());
        assertTrue(node.get("createdAt").isNumber());

        SamplePayload parsed = JsonUtil.parseObject(blob, SamplePayload.class);
        assertPayloadEquals(original, parsed);
    }

    @Test
    @DisplayName("JsonUtil 序列化 → old Jackson2 反序列化（模拟新客户端调老服务）")
    void newSerializeOldDeserialize() throws Exception {
        SamplePayload original = sample();
        String blob = JsonUtil.toJSONString(original);

        SamplePayload parsed = legacyJackson2().readValue(blob, SamplePayload.class);
        assertPayloadEquals(original, parsed);
    }

    @Test
    @DisplayName("模拟 Redis STRING：JsonUtil 写存读")
    void redisStringSimulationJsonUtil() {
        SamplePayload original = sample();
        String stored = JsonUtil.toJSONString(original);
        SamplePayload loaded = JsonUtil.parseObject(stored, SamplePayload.class);
        assertPayloadEquals(original, loaded);
        assertNull(loaded.getNote());
    }

    private static void assertPayloadEquals(SamplePayload expected, SamplePayload actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUpdatedAt(), actual.getUpdatedAt());
        assertEquals(expected.getCreatedAt().getTime(), actual.getCreatedAt().getTime());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getTier(), actual.getTier());
        assertEquals(expected.getNested().getRef(), actual.getNested().getRef());
        assertEquals(expected.getNested().getWeight(), actual.getNested().getWeight(), 1e-9);
        assertEquals(expected.getLabels(), actual.getLabels());
        assertEquals(expected.getCounts(), actual.getCounts());
        assertEquals(0, expected.getRatio().compareTo(actual.getRatio()));
        assertEquals(expected.getToken(), actual.getToken());
    }
}
