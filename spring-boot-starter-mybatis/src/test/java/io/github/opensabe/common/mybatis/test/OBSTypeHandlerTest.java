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
package io.github.opensabe.common.mybatis.test;

import io.github.opensabe.common.dynamodb.service.KeyValueDynamoDbService;
import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import io.github.opensabe.common.mybatis.test.common.BaseMybatisTest;
import io.github.opensabe.common.mybatis.test.mapper.user.OrderMapper;
import io.github.opensabe.common.mybatis.test.po.DynamodbPO;
import io.github.opensabe.common.mybatis.test.po.Order;
import io.github.opensabe.common.s3.typehandler.S3OBSService;
import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OBS类型处理器测试")
public class OBSTypeHandlerTest extends BaseMybatisTest {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private S3OBSService s3OBSService;
    @Autowired
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    @Test
    @SneakyThrows
    @DisplayName("测试OBS类型处理器 - 验证数据存储和查询")
    public void create () {
        var order = new Order();
        order.setId("OBSTypeHandlerTestOrder1");
        var info = new Order.OrderInfo();
        info.setStake(5000);
        info.setStockId("OBSTypeHandlerTestStock111111");
        order.setOrderInfo(info);
        orderMapper.insertSelective(order);

        // 测试 Mapper 内置查询
        var db = orderMapper.selectByPrimaryKey(order.getId());
        assertThat(db).matches(
                order1 -> order1.getId().equals(order.getId()) &&
                        order1.getOrderInfo().getStake().equals(order.getOrderInfo().getStake()) &&
                        order1.getOrderInfo().getStockId().equals(order.getOrderInfo().getStockId())
        );

        // 测试 Mapper 自定义查询
        var db2 = orderMapper.selectByMapper(order.getId());
        assertThat(db2).matches(
                order1 -> order1.getId().equals(order.getId()) &&
                        order1.getOrderInfo().getStake().equals(order.getOrderInfo().getStake()) &&
                        order1.getOrderInfo().getStockId().equals(order.getOrderInfo().getStockId())
        );

        // 测试直接从数据库查询，验证原始数据的实现，即数据库中存储的是 key，具体数据在 s3 中
        try (
            var connection = dynamicRoutingDataSource.getConnection();
            var statement = connection.createStatement()
        ) {
            statement.execute("select * from t_order where id = '" + order.getId() + "'");
            var resultSet = statement.getResultSet();
            while (resultSet.next()) {
                // 直接从ResultSet中获取order_info字段，数据库应该是 key
                // value 保存在 dynamodb，用 key 唯一标识
                var orderInfo = resultSet.getObject("order_info", String.class);
                String select = s3OBSService.select(orderInfo);
                Order.OrderInfo parsedObject = JsonUtil.parseObject(select, Order.OrderInfo.class);
                assertThat(parsedObject).matches(
                        orderInfo1 -> Objects.equals(orderInfo1.getStake(), info.getStake()) &&
                                orderInfo1.getStockId().equals(info.getStockId())
                );
            }
        }
    }

}
