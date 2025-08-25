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

import org.apache.ibatis.type.JdbcType;

import io.github.opensabe.common.mybatis.types.S3TypeHandler;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import tk.mybatis.mapper.annotation.ColumnType;


@Getter
@Setter
@Table(name = "t_order")
public class Order {

    @Id
    private String id;

    @ColumnType(jdbcType = JdbcType.VARCHAR, typeHandler = S3TypeHandler.class)
    private OrderInfo orderInfo;

    @Getter
    @Setter
    public static class OrderInfo {
        private String stockId;

        private Integer stake;
    }
}
