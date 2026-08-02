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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;

/**
 * {@link JSONTypeHandler} Jackson 3 契约：独立 ObjectMapper、未知字段忽略与 null 写入。
 */
@DisplayName("JSONTypeHandler Jackson 契约")
class JSONTypeHandlerContractTest {

    private static final String COL = "col";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TestPojo SAMPLE = new TestPojo("k", "v", List.of(new TestPojo("k2", "v2", null)));

/** rs。 */
    private ResultSet rs;

    @BeforeEach
    void setup() throws Exception {
        rs = Mockito.mock(ResultSet.class);
    }

    /**
     * 写入 JSON 字符串后须可用 Jackson 原样解析嵌套结构。
     */
    @Test
    @DisplayName("反序列化忽略未知字段；blank / null 返回 null")
    void writeThenReadWithJackson() throws Exception {
        AtomicReference<String> written = new AtomicReference<>();
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        doAnswer(inv -> {
            written.set(inv.getArgument(1));
            return null;
        }).when(ps).setString(anyInt(), anyString());

        JSONTypeHandler handler = new JSONTypeHandler(TestPojo.class);
        handler.setNonNullParameter(ps, 1, SAMPLE, JdbcType.VARCHAR);

        JsonNode node = MAPPER.readTree(written.get());
        assertEquals("k", node.get("key").asText());
        assertEquals("v", node.get("val").asText());
        assertEquals(1, node.get("children").size());

        TestPojo roundTrip = MAPPER.readValue(written.get(), TestPojo.class);
        assertEquals(SAMPLE.getKey(), roundTrip.getKey());
        assertEquals(SAMPLE.getVal(), roundTrip.getVal());
        assertEquals(SAMPLE.getChildren().get(0).getKey(), roundTrip.getChildren().get(0).getKey());
    }

    /**
     * 反序列化忽略未知字段；blank/null 列值返回 null。
     */
    @Test
    void readUnknownFieldsAndNulls() throws Exception {
        JSONTypeHandler handler = new JSONTypeHandler(TestPojo.class);

        Mockito.when(rs.getString(COL)).thenReturn("{\"key\":\"a\",\"val\":\"b\",\"unknown\":1}");
        TestPojo pojo = (TestPojo) handler.getNullableResult(rs, COL);
        assertEquals("a", pojo.getKey());
        assertEquals("b", pojo.getVal());

        Mockito.when(rs.getString(COL)).thenReturn("  ");
        assertNull(handler.getNullableResult(rs, COL));

        Mockito.when(rs.getString(COL)).thenReturn(null);
        assertNull(handler.getNullableResult(rs, COL));
    }

    /**
     * null 参数经 handler 写入 JDBC null 字符串。
     */
    @Test
    void writeNull() throws Exception {
        AtomicReference<String> written = new AtomicReference<>("sentinel");
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        doAnswer(inv -> {
            written.set(inv.getArgument(1));
            return null;
        }).when(ps).setString(anyInt(), isNull());

        new JSONTypeHandler(TestPojo.class).setNonNullParameter(ps, 1, null, JdbcType.VARCHAR);
        assertNull(written.get());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestPojo {
/** key。 */
        private String key;
/** val。 */
        private String val;
/** children。 */
        private List<TestPojo> children;
    }
}
