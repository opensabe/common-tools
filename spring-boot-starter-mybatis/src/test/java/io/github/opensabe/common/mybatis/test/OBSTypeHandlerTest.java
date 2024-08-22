package io.github.opensabe.common.mybatis.test;

import io.github.opensabe.common.mybatis.test.common.BaseMybatisTest;
import io.github.opensabe.common.mybatis.test.mapper.user.OrderMapper;
import io.github.opensabe.common.mybatis.test.po.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class OBSTypeHandlerTest extends BaseMybatisTest {
    @Autowired
    private OrderMapper orderMapper;

    @Test
    @Transactional
    public void create () {
        orderMapper.deleteByPrimaryKey("order1");
        var order = new Order();
        order.setId("order1");
        var info = new Order.OrderInfo();
        info.setMarket(1);
        info.setMatchId("xxxxx111111111");
        order.setOrderInfo(info);
        orderMapper.insertSelective(order);

        var db = orderMapper.selectByPrimaryKey("order1");
        Assertions.assertEquals(db.getOrderInfo().getMatchId(), "xxxxx111111111");
    }

}
