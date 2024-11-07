package io.github.opensabe.common.mybatis.test;

import io.github.opensabe.common.mybatis.test.common.BaseMybatisWithValkeyTest;
import io.github.opensabe.common.mybatis.test.mapper.user.OrderMapper;
import io.github.opensabe.common.mybatis.test.po.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Disabled
public class OBSTypeHandlerWithValkeyTest extends BaseMybatisWithValkeyTest {
    @Autowired
    private OrderMapper orderMapper;

    @Test
    @Transactional
    public void create () {
        orderMapper.deleteByPrimaryKey("order2");
        var order = new Order();
        order.setId("order2");
        var info = new Order.OrderInfo();
        info.setMarket(1);
        info.setMatchId("xxxxx222222222");
        order.setOrderInfo(info);
        orderMapper.insertSelective(order);

        var db = orderMapper.selectByPrimaryKey("order2");
        Assertions.assertEquals(db.getOrderInfo().getMatchId(), "xxxxx222222222");
    }

}
