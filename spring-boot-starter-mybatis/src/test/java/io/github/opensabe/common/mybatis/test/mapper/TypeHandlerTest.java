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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.alibaba.fastjson.JSONObject;

import io.github.opensabe.common.mybatis.types.JSONTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;

@DisplayName("MyBatis类型处理器测试")
public class TypeHandlerTest {

    private static final String COL_NAME = "Test";
    private static final TestPOJO TEST_POJO =
            new TestPOJO("key", "val", List.of(new TestPOJO("key", "val", null)));
    ResultSet rs;

    @BeforeEach
    public void setup() throws Exception {
        rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(true).thenReturn(false);
    }

    @Test
    @DisplayName("测试JSON类型处理器 - 插入JSON对象")
    public void testJSONTypeHandler_InsertJSON_ExpectInserted() throws Exception {
        AtomicReference<String> actual = new AtomicReference<>();
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        doAnswer(inv -> {
            actual.set(inv.getArgument(1));
            return null;
        })
                .when(ps)
                .setString(anyInt(), anyString());

        new JSONTypeHandler(TestPOJO.class).setNonNullParameter(ps, 1, TEST_POJO, JdbcType.NCHAR);

        assertEqual(TEST_POJO, JSONObject.parseObject(actual.get(), TestPOJO.class));
    }

    @Test
    @DisplayName("测试JSON类型处理器 - 插入null值")
    public void testJSONTypeHandler_InsertNull_ExpectNull() throws Exception {
        AtomicReference<String> res = new AtomicReference<>();
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        doAnswer(inv -> {
            res.set(inv.getArgument(1));
            return null;
        })
                .when(ps)
                .setString(anyInt(), anyString());

        new JSONTypeHandler(TestPOJO.class).setNonNullParameter(ps, 1, null, JdbcType.NCHAR);

        Assertions.assertNull(res.get());

    }

    @Test
    @DisplayName("测试JSON类型处理器 - 正常JSON反序列化")
    public void testJSONTypeHandler_NormalJSON_ExpectMatch() throws Exception {
        Mockito.when(rs.getString(COL_NAME)).thenReturn(JSONObject.toJSONString(TEST_POJO));
        TestPOJO actual = (TestPOJO) new JSONTypeHandler(TestPOJO.class).getNullableResult(rs, COL_NAME);
        assertEqual(TEST_POJO, actual);
    }

    @Test
    @DisplayName("测试JSON类型处理器 - null值反序列化")
    public void testJSONTypeHandler_NullValue_ExpectNull() throws Exception {
        Mockito.when(rs.getString(COL_NAME)).thenReturn(null);
        TestPOJO res = (TestPOJO) new JSONTypeHandler(TestPOJO.class).getNullableResult(rs, COL_NAME);
        Assertions.assertNull(res);
    }

    private void assertEqual(TestPOJO expect, TestPOJO actual) {
        Assertions.assertEquals(expect.getKey(), actual.getKey());
        Assertions.assertEquals(expect.getVal(), actual.getVal());

        if (expect.getTestPOJOS() != null) {
            for (int i = 0; i < expect.getTestPOJOS().size(); i++) {
                assertEqual(expect.getTestPOJOS().get(i), actual.testPOJOS.get(i));
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestPOJO {
        private String key;
        private String val;
        private List<TestPOJO> testPOJOS;
    }
}
