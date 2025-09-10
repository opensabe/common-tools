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
package io.github.opensabe.spring.boot.starter.rocketmq;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.opensabe.common.config.dal.db.dao.MqFailLogEntityMapper;
import io.github.opensabe.common.config.dal.db.entity.MqFailLogEntity;
import io.github.opensabe.common.testcontainers.integration.SingleRedisIntegrationTest;
import io.github.opensabe.common.testcontainers.integration.SingleWriteMySQLIntegrationTest;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.boot.starter.rocketmq.test.common.BaseRocketMQTest;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AutoConfigureObservability
@JfrEventTest
@ExtendWith({
        SpringExtension.class,
        SingleRedisIntegrationTest.class,
        SingleWriteMySQLIntegrationTest.class
})
@SpringBootTest(classes = BaseRocketMQTest.App.class)
@Import(MessageSaveTest.Config.class)
@DisplayName("RocketMQ消息保存测试")
public class MessageSaveTest {

    private static final String COUNT_SQL = "select count(1) from t_common_mq_fail_log";
    private static final String QUERY_SQL = "select * from t_common_mq_fail_log";
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private io.github.opensabe.spring.boot.starter.rocketmq.MQProducer producer;
    @Autowired
    private MqFailLogEntityMapper mqFailLogEntityMapper;

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleRedisIntegrationTest.setProperties(registry);
        SingleWriteMySQLIntegrationTest.setProperties(registry);
    }

    @Test
    @DisplayName("测试消息失败保存 - 验证失败消息存储到数据库")
    void test1() throws SQLException {
        try (
                var session = sqlSessionFactory.openSession();
                var conn = session.getConnection();
                var stmt = conn.prepareCall(COUNT_SQL);
                var rs = stmt.executeQuery()
        ) {
            Assertions.assertTrue(rs.next());
            Assertions.assertEquals(0, rs.getInt(1));
        }

        String topic = "testTopic";
        String message = "xxxx";
        producer.send(topic, message);

        try (
                var session = sqlSessionFactory.openSession();
                var conn = session.getConnection();
                var stmt = conn.prepareCall(QUERY_SQL);
                var rs = stmt.executeQuery()
        ) {
            Assertions.assertTrue(rs.next());
            MqFailLogEntity entity = fromResultSet(rs);
            log.info(JsonUtil.toJSONString(entity));
            Assertions.assertNotNull(entity);
            Assertions.assertEquals(topic, entity.getTopic());
        }

        //验证定时任务 SQL
        var list = mqFailLogEntityMapper.selectPendingMessages(1000);
        Assertions.assertEquals(1, list.size());
        var entity = list.get(0);
        mqFailLogEntityMapper.updateStatusAndRetryNum(entity.getId(), MqFailLogEntity.STATUS_SUCCESS, entity.getRetryNum() + 1);
        list = mqFailLogEntityMapper.selectPendingMessages(1000);
        Assertions.assertEquals(0, list.size());
    }

    private MqFailLogEntity fromResultSet(ResultSet resultSet) throws SQLException {
        MqFailLogEntity entity = new MqFailLogEntity();
        entity.setId(resultSet.getString("id"));
        entity.setTopic(resultSet.getString("topic"));
        entity.setBody(resultSet.getString("body"));
        entity.setSendStatus(resultSet.getInt("send_status"));
        entity.setRetryNum(resultSet.getInt("retry_num"));
        entity.setTraceId(resultSet.getString("trace_id"));
        entity.setSendConfig(resultSet.getString("send_config"));
        return entity;
    }

    public static class Config {

        @Bean
        public RocketMQTemplate rocketMQTemplate() {
            var r = new RocketMQTemplate() {

                @Override
                public SendResult syncSend(String destination, Message<?> message) {
                    return new SendResult(SendStatus.FLUSH_DISK_TIMEOUT, "id3", "id1", null, 100);
                }
            };
            r.setProducer(new TransactionMQProducer("rocketmq-test"));
            return r;
        }
    }
}
