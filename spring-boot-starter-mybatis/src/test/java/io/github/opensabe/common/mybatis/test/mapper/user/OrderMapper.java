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
package io.github.opensabe.common.mybatis.test.mapper.user;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import io.github.opensabe.common.mybatis.base.BaseMapper;
import io.github.opensabe.common.mybatis.test.po.Order;
import io.github.opensabe.common.mybatis.types.S3TypeHandler;

/**
 * 订单 MyBatis Mapper 测试接口。
 */
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 按主键查询订单（{@code order_info} 经 {@link S3TypeHandler} 映射）。
     *
     * @param id 订单 ID
     * @return 订单 PO，不存在为 {@code null}
     */
    @Results(
            {
                    @Result(column = "order_info", property = "orderInfo", typeHandler = S3TypeHandler.class)
            }
    )
    @Select("select * from t_order where id = #{id}")
    Order selectByMapper(@Param("id") String id);
}
