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
package io.github.opensabe.common.mybatis.types;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * 将 JDBC 列与 Java 对象通过 JSON 字符串互转的 MyBatis {@link org.apache.ibatis.type.TypeHandler}。
 * <p>
 * POJO 字段不支持裸泛型：基本类型元素集合（如 {@code List<Integer>}）可直接使用；复杂元素泛型需子类包装，例如：
 * </p>
 * <pre class="code">
 * public class Displays extends ArrayList&lt;DisplaySetting&gt; { }
 * public class Configs extends HashMap&lt;String, DisplaySetting&gt; { }
 * </pre>
 * <p>POJO 示例：</p>
 * <pre class="code">
 * public class Activity {
 *     &#064;ColumnType(typeHandler = JSONTypeHandler.class)
 *     private List&lt;Integer&gt; activityDesc;
 *
 *     &#064;ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = JSONTypeHandler.class)
 *     private Displays displaySetting;
 *
 *     &#064;ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = JSONTypeHandler.class)
 *     private Configs commonConfig;
 * }
 * </pre>
 * <p>Mapper 示例：</p>
 * <pre class="code">
 * public interface ActivityMapper {
 *     &#064;Results({
 *         &#064;Result(column = "activity_desc", property = "activityDesc", typeHandler = JSONTypeHandler.class),
 *         &#064;Result(column = "display_setting", property = "displaySetting", typeHandler = JSONTypeHandler.class)
 *     })
 *     &#064;Select("select * from activity")
 *     List&lt;Activity&gt; select();
 * }
 * </pre>
 * <p>
 * 使用独立最小 Jackson 3 {@link JsonMapper}（仅关闭 {@code FAIL_ON_UNKNOWN_PROPERTIES}）；
 * DB JSON 日期偏 ISO，与 {@code JsonUtil} epoch-ms 及 HTTP Jackson 3 配置分离。
 * </p>
 */
public class JSONTypeHandler extends BaseTypeHandler<Object> {

    /**
     * 目标 Java 类型（由 MyBatis 构造器注入）。
     */
    private final Class<?> type;

    /**
     * 专用于 DB JSON 读写的 ObjectMapper，不共享 HTTP/JsonUtil 配置。
     */
    private final ObjectMapper objectMapper;

    /**
     * @param type 反序列化目标类型
     */
    public JSONTypeHandler(Class<?> type) {
        this.type = type;
        this.objectMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    /**
     * 将非 null 参数序列化为 JSON 字符串写入 PreparedStatement。
     *
     * @param ps        PreparedStatement
     * @param i         参数下标
     * @param parameter 参数值
     * @param jdbcType  JDBC 类型
     * @throws SQLException JDBC 异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setString(i, null);
        } else if (parameter instanceof String) {
            ps.setString(i, (String) parameter);
        } else {
            ps.setString(i, toString(parameter));
        }
    }

    /**
     * 按列名读取 JSON 并反序列化。
     *
     * @param rs         ResultSet
     * @param columnName 列名
     * @return Java 对象，空串/空白为 {@code null}
     * @throws SQLException JDBC 异常
     */
    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toJavaBean(rs.getString(columnName));
    }

    /**
     * 按列下标读取 JSON 并反序列化。
     *
     * @param rs          ResultSet
     * @param columnIndex 列下标
     * @return Java 对象，空串/空白为 {@code null}
     * @throws SQLException JDBC 异常
     */
    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toJavaBean(rs.getString(columnIndex));
    }

    /**
     * 从存储过程结果按列下标读取 JSON 并反序列化。
     *
     * @param cs          CallableStatement
     * @param columnIndex 列下标
     * @return Java 对象，空串/空白为 {@code null}
     * @throws SQLException JDBC 异常
     */
    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toJavaBean(cs.getString(columnIndex));
    }

    /**
     * 将 JSON 字符串反序列化为 {@link #type}；{@link String} 类型直接返回原文。
     *
     * @param content 列 JSON 文本
     * @return 反序列化结果，空白输入为 {@code null}
     * @throws RuntimeException Jackson 解析失败时包装抛出
     */
    protected Object toJavaBean(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        if (String.class.isAssignableFrom(type)) {
            return content;
        }
        try {
            return objectMapper.readValue(content, type);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象序列化为 JSON 字符串；{@link String} 实例直接 {@code toString()}。
     *
     * @param object 待序列化对象
     * @return JSON 字符串，{@code null} 输入为 {@code null}
     * @throws RuntimeException Jackson 序列化失败时包装抛出
     */
    protected String toString(Object object) {
        if (Objects.isNull(object)) {
            return null;
        }
        if (object instanceof String) {
            return object.toString();
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }
}
