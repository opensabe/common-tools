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

import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.opensabe.common.dynamodb.service.KeyValueDynamoDbService;
import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import io.github.opensabe.common.mybatis.test.common.BaseMybatisTest;
import io.github.opensabe.common.mybatis.test.mapper.user.DynamodbTypeHandlerMapper;
import io.github.opensabe.common.mybatis.test.po.DynamodbPO;
import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.SneakyThrows;

@DisplayName("DynamoDB类型处理器测试")
public class DynamodbTypeHandlerTest extends BaseMybatisTest {
    @Autowired
    private DynamodbTypeHandlerMapper dynamodbTypeHandlerMapper;
    @Autowired
    private KeyValueDynamoDbService keyValueDynamoDbService;
    @Autowired
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    @Test
    @SneakyThrows
    @DisplayName("测试DynamoDB类型处理器 - 验证数据存储和查询")
    public void testDynamodyTypeHandler() {
        var dynamodbPO = new DynamodbPO();
        dynamodbPO.setId("DynamodbTypeHandlerTestPO1");
        var info = new DynamodbPO.OrderInfo();
        info.setStake(100);
        info.setStockId("DynamodbTypeHandlerTestStockId222222");
        dynamodbPO.setOrderInfo(info);
        dynamodbTypeHandlerMapper.insertSelective(dynamodbPO);

        //测试 Mapper 内置查询
        var db = dynamodbTypeHandlerMapper.selectByPrimaryKey(dynamodbPO.getId());
        assertThat(db).matches(
                dynamodbPO1 -> dynamodbPO1.getId().equals(dynamodbPO.getId()) &&
                        Objects.equals(dynamodbPO1.getOrderInfo().getStake(), dynamodbPO.getOrderInfo().getStake()) &&
                        dynamodbPO1.getOrderInfo().getStockId().equals(dynamodbPO1.getOrderInfo().getStockId())
        );
        // 测试 Mapper 自定义查询
        var db2 = dynamodbTypeHandlerMapper.selectByMapper(dynamodbPO.getId());
        assertThat(db2).matches(
                dynamodbPO1 -> dynamodbPO1.getId().equals(dynamodbPO.getId()) &&
                        Objects.equals(dynamodbPO1.getOrderInfo().getStake(), dynamodbPO.getOrderInfo().getStake()) &&
                        dynamodbPO1.getOrderInfo().getStockId().equals(dynamodbPO1.getOrderInfo().getStockId())
        );

        // 测试直接从数据库查询，验证原始数据的实现，即数据库中存储的是 key，具体数据在 dynamodb 中
        try (
                Connection connection = dynamicRoutingDataSource.getConnection();
                Statement statement = connection.createStatement()
        ) {
            statement.execute("select * from t_dynamodb_type_handler where id = '" + dynamodbPO.getId() + "'");
            var resultSet = statement.getResultSet();
            while (resultSet.next()) {
                // 直接从ResultSet中获取order_info字段，数据库应该是 key
                // value 保存在 dynamodb，用 key 唯一标识
                var orderInfo = resultSet.getObject("order_info", String.class);
                KeyValueDynamoDbService.KeyValueMap keyValueMap = new KeyValueDynamoDbService.KeyValueMap();
                keyValueMap.setKey(orderInfo);
                KeyValueDynamoDbService.KeyValueMap result = keyValueDynamoDbService.selectOne(keyValueMap);
                DynamodbPO.OrderInfo parsedObject = JsonUtil.parseObject(result.getValue(), DynamodbPO.OrderInfo.class);
                assertThat(parsedObject).matches(
                        orderInfo1 -> Objects.equals(orderInfo1.getStake(), info.getStake()) &&
                                orderInfo1.getStockId().equals(info.getStockId())
                );
            }
        }

    }

}
