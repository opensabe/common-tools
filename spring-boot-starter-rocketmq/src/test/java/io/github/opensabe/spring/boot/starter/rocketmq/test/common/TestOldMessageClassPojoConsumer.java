package io.github.opensabe.spring.boot.starter.rocketmq.test.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.entity.base.vo.BaseMessage;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.boot.starter.rocketmq.AbstractConsumer;
import io.github.opensabe.spring.boot.starter.rocketmq.AbstractMQConsumer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RocketMQMessageListener(
        consumerGroup = "${spring.application.name}_" + TestDataUtil.TEST_CLASS_TOPIC + "-test-old-consumer",
        topic = TestDataUtil.TEST_CLASS_TOPIC
)
public class TestOldMessageClassPojoConsumer extends AbstractMQConsumer {

    @Getter
    private final List<MessageClassPojoWrapper> MessageClassPojoWrappers = new CopyOnWriteArrayList<>();

    @Override
    protected void onBaseMQMessage(BaseMQMessage baseMQMessage) {
        String data = baseMQMessage.getData();
        MessageClassPojo messageClassPojo = JsonUtil.parseObject(data, MessageClassPojo.class);
        MessageClassPojoWrappers.forEach(MessageClassPojoWrapper -> {
            if (MessageClassPojoWrapper.getMessageClassPojo().equals(messageClassPojo)) {
                log.info("Received old message: {}", messageClassPojo.getTimestamp());
                MessageClassPojoWrapper.getCountDownLatch().countDown();
            }
        });
    }
}