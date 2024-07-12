package io.github.opensabe.common.mybatis.test;

import io.github.opensabe.common.mybatis.test.mapper.user.OrderMapper;
import io.github.opensabe.common.mybatis.test.po.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class OBSTypeHandlerTest extends BaseDataSourceTest {
    @Autowired
    private OrderMapper orderMapper;

    @Test
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
        System.out.println(db.getOrderInfo().getMatchId());
    }

}
