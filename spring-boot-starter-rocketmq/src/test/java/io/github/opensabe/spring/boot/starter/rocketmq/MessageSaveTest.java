package io.github.opensabe.spring.boot.starter.rocketmq;

import io.github.opensabe.common.config.dal.db.dao.MqFailLogEntityMapper;
import io.github.opensabe.common.config.dal.db.dao.MqFailLogEntitySqlProvider;
import io.github.opensabe.common.config.dal.db.entity.MqFailLogEntity;
import io.github.opensabe.spring.boot.starter.rocketmq.test.common.BaseRocketMQTest;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class MessageSaveTest extends BaseRocketMQTest {


    @Autowired
    private MqFailLogEntityMapper mapper;


    @Test
    void test1 () {
        MqFailLogEntitySqlProvider provider = new MqFailLogEntitySqlProvider();

        MqFailLogEntity entity = new MqFailLogEntity();

        entity.setId("1");
        entity.setTopic("22");
        entity.setBody("xx");
        entity.setHashKey("ss");
        entity.setRetryNum(1);
        entity.setSendConfig("xx");
        entity.setSendStatus(1);
        entity.setTraceId("xxx");

        String sql = provider.insertSelective(entity);
        System.out.printf(sql);


        mapper.insertSelective(entity);

    }
}
