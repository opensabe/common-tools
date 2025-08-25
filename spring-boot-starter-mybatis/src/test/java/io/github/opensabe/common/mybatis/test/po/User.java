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
package io.github.opensabe.common.mybatis.test.po;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.ibatis.type.JdbcType;

import io.github.opensabe.common.mybatis.types.JSONTypeHandler;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tk.mybatis.mapper.annotation.ColumnType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_user")
public class User {

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private Timestamp createTime;
    @ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = JSONTypeHandler.class)
    private Properties properties;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Property {
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
}
