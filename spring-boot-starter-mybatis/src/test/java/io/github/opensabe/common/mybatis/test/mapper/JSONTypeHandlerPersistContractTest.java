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
package io.github.opensabe.common.mybatis.test.mapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import io.github.opensabe.common.mybatis.types.JSONTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

/**
 * {@link JSONTypeHandler} 落库线格式契约：LocalDateTime 为 ISO 而非 JsonUtil epoch-ms。
 */
@DisplayName("JSONTypeHandler 落库 ISO 日期持久化契约")
class JSONTypeHandlerPersistContractTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 写读 round-trip；LocalDateTime 须为 ISO 文本而非 epoch 数字。
     */
    @Test
    @DisplayName("写读 round-trip；LocalDateTime 线格式为 ISO 而非 epoch")
    void writeReadIsoLocalDateTime() throws Exception {
        ConfigBlob original = new ConfigBlob(
                LocalDateTime.of(2026, 1, 15, 10, 30, 0),
                LocalDateTime.of(2026, 1, 15, 11, 0, 0),
                Map.of("enabled", true, "tier", 2)
        );

        AtomicReference<String> column = new AtomicReference<>();
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        doAnswer(inv -> {
            column.set(inv.getArgument(1));
            return null;
        }).when(ps).setString(anyInt(), anyString());

        JSONTypeHandler handler = new JSONTypeHandler(ConfigBlob.class);
        handler.setNonNullParameter(ps, 1, original, JdbcType.VARCHAR);

        JsonNode node = MAPPER.readTree(column.get());
        assertTrue(node.get("windowStart").isTextual() || node.get("windowStart").asText().contains("T")
                || node.get("windowStart").asText().contains("-"));
        assertFalse(node.get("windowStart").isNumber(), "JSONTypeHandler must not use JsonUtil epoch-ms");

        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString("col")).thenReturn(column.get());
        ConfigBlob loaded = (ConfigBlob) handler.getNullableResult(rs, "col");
        assertEquals(original.getWindowStart(), loaded.getWindowStart());
        assertEquals(original.getWindowEnd(), loaded.getWindowEnd());
        assertEquals(original.getOptions().get("tier"), loaded.getOptions().get("tier"));
    }

    /**
     * 模拟升级前已落库的 ISO 字串仍可读。
     */
    @Test
    @DisplayName("旧库 ISO 字串可读（模拟升级前已落库）")
    void legacyIsoColumnReadable() throws Exception {
        String legacy = """
                {"windowStart":"2026-01-15T10:30:00","windowEnd":"2026-01-15T11:00:00","options":{"enabled":true,"tier":2}}
                """;
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString("col")).thenReturn(legacy.trim());

        ConfigBlob loaded = (ConfigBlob) new JSONTypeHandler(ConfigBlob.class).getNullableResult(rs, "col");
        assertEquals(LocalDateTime.of(2026, 1, 15, 10, 30), loaded.getWindowStart());
        assertEquals(2, ((Number) loaded.getOptions().get("tier")).intValue());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigBlob {
        private LocalDateTime windowStart;
        private LocalDateTime windowEnd;
        private Map<String, Object> options;
    }
}
