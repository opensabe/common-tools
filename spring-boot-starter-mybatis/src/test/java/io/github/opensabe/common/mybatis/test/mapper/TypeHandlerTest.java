package io.github.opensabe.common.mybatis.test.mapper;

import com.alibaba.fastjson.JSONObject;
import io.github.opensabe.common.mybatis.types.JSONTypeHandler;
import lombok.*;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.*;

public class TypeHandlerTest {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestPOJO {
        private String key;
        private String val;
        private List<TestPOJO> testPOJOS;
    }

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
    public void testJSONTypeHandler_InsertJSON_ExpectInserted() throws Exception {
        AtomicReference<String> actual = new AtomicReference<>();
        PreparedStatement ps = Mockito.mock(PreparedStatement .class);
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
    public void testJSONTypeHandler_InsertNull_ExpectNull() throws Exception {
        AtomicReference<String> res = new AtomicReference<>();
        PreparedStatement ps = Mockito.mock(PreparedStatement .class);
        doAnswer(inv -> {
            res.set(inv.getArgument(1));
            return null;
        })
                .when(ps)
                .setString(anyInt(), anyString());

        new JSONTypeHandler(TestPOJO.class).setNonNullParameter(ps, 1,null, JdbcType.NCHAR);

        Assertions.assertNull(res.get());

    }

    @Test
    public void testJSONTypeHandler_NormalJSON_ExpectMatch() throws Exception {
        Mockito.when(rs.getString(COL_NAME)).thenReturn(JSONObject.toJSONString(TEST_POJO));
        TestPOJO actual = (TestPOJO) new JSONTypeHandler(TestPOJO.class).getNullableResult(rs, COL_NAME);
        assertEqual(TEST_POJO, actual);
    }

    @Test
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
}
