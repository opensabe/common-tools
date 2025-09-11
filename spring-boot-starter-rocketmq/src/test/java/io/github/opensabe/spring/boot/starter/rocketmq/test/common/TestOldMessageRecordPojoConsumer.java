package io.github.opensabe.spring.boot.starter.rocketmq.test.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.boot.starter.rocketmq.AbstractMQConsumer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RocketMQMessageListener(
        consumerGroup = "${spring.application.name}_" + TestDataUtil.TEST_RECORD_TOPIC + "-test-old-consumer",
        topic = TestDataUtil.TEST_RECORD_TOPIC
)
public class TestOldMessageRecordPojoConsumer extends AbstractMQConsumer {

    @Getter
    private final List<MessageRecordPojoWrapper> MessageRecordPojoWrappers = new CopyOnWriteArrayList<>();

    @Override
    protected void onBaseMQMessage(BaseMQMessage baseMQMessage) {
        String data = baseMQMessage.getData();
        MessageRecordPojo MessageRecordPojo = JsonUtil.parseObject(data, MessageRecordPojo.class);
        MessageRecordPojoWrappers.forEach(MessageRecordPojoWrapper -> {
            if (MessageRecordPojoWrapper.getMessageRecordPojo().equals(MessageRecordPojo)) {
                log.info("Received old message: {}", MessageRecordPojo.timestamp());
                MessageRecordPojoWrapper.getCountDownLatch().countDown();
            }
        });
    }
}