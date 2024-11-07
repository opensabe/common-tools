package io.github.opensabe.common.mybatis.test;

import io.github.opensabe.common.mybatis.test.common.BaseMybatisWithValkeyTest;
import io.github.opensabe.common.mybatis.test.mapper.user.DynamodbTypeHandlerMapper;
import io.github.opensabe.common.mybatis.test.po.DynamodbPO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Disabled
public class DynamodbTypeHandlerWithValkeyTest extends BaseMybatisWithValkeyTest {
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
