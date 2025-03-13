package io.github.opensabe.common.mybatis.test.po;

import io.github.opensabe.common.mybatis.types.JSONTypeHandler;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;
import tk.mybatis.mapper.annotation.ColumnType;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_user")
public class User {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Property{
        private String key;
        private String value;
        private Properties properties;
    }

    @NoArgsConstructor
    public static class Properties extends ArrayList<Property> {
        public Properties(Collection<Property> es) {
            super(es);
        }
    }

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private Timestamp createTime;
    @ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = JSONTypeHandler.class)
    private Properties properties;
}
