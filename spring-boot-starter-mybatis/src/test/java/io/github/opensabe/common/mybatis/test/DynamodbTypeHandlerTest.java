package io.github.opensabe.common.mybatis.test;

import io.github.opensabe.common.mybatis.test.common.BaseMybatisTest;
import io.github.opensabe.common.mybatis.test.mapper.user.DynamodbTypeHandlerMapper;
import io.github.opensabe.common.mybatis.test.po.DynamodbPO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.sql.Connection;
import java.sql.Statement;

public class DynamodbTypeHandlerTest extends BaseMybatisTest {
    @Autowired
    private DynamodbTypeHandlerMapper dynamodbTypeHandlerMapper;

    @Test
    @Transactional
    public void testDynamodyTypeHandler () {
//        dynamodbTypeHandlerMapper.deleteByPrimaryKey("order1");
        var dynamodbPO = new DynamodbPO();
        dynamodbPO.setId("order1");
        var info = new DynamodbPO.OrderInfo();
        info.setMarket(1);
        info.setMatchId("222222");
        dynamodbPO.setOrderInfo(info);
        dynamodbTypeHandlerMapper.insertSelective(dynamodbPO);
        var db = dynamodbTypeHandlerMapper.selectByPrimaryKey("order1");
        Assertions.assertEquals(db.getOrderInfo().getMatchId(), "222222");
    }

}
