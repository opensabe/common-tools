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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * pojo里不支持泛型，如果遇到泛型需要用子类包一层，如果泛型类型为基本类型，则直接用：
 * <p>
 * 支持 List&lt;Integer> 但是不支持 List&lt;DisplaySetting> 需要写一个子类
 * <pre class="code">
 *      public class Displays extends ArrayList&lt;DisplaySetting> {
 *
 *      }
 *      public class Configs extends HashMap&lt;String,DisplaySetting> {
 *
 *      }
 *  <pre/>
 *
 * <p>example for pojo:</p>
 * <pre class="code">
 *  public class Activity {
 *     &#064;ColumnType(typeHandler = JSONTypeHandler.class)
 *     private List&lt;Integer&gt; activityDesc;
 *
 *     &#064;ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = JSONTypeHandler.class)
 *     private Displays displaySetting;
 *
 *     &#064;ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = JSONTypeHandler.class)
 *     private Configs commonConfig;
 *  }
 * </pre>
 *
 * <p>example for mapper:</p>
 * <pre class="code">
 *  public interface ActivityMapper {
 *
 *      &#064;Results(
 *          {
 *              &#064;Result(column = "activity_desc",property = "activityDesc",typeHandler = JSONTypeHandler.class),
 *              &#064;Result(column = "display_setting",property = "DisplaySetting",typeHandler = JSONTypeHandler.class)
 *          }
 *      )
 *      &#064;select{"select * from activity"}
 *      List&lt;Activity&gt; select();
 *  }
 * </pre>
 *
 */
public class JSONTypeHandler extends BaseTypeHandler<Object> {

    private final Class<?> type;
    private final ObjectMapper objectMapper;

    public JSONTypeHandler(Class<?> type) {
        this.type = type;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }


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

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toJavaBean(rs.getString(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toJavaBean(rs.getString(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toJavaBean(cs.getString(columnIndex));
    }

    protected Object toJavaBean(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        if (String.class.isAssignableFrom(type)) {
            return content;
        }
        try {
            return objectMapper.readValue(content, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected String toString(Object object) {
        if (Objects.isNull(object)) {
            return null;
        }
        if (object instanceof String) {
            return object.toString();
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
