package io.github.opensabe.spring.boot.starter.rocketmq.test.common;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import io.github.opensabe.common.entity.base.vo.BaseMessage;
import io.github.opensabe.spring.boot.starter.rocketmq.AbstractConsumer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RocketMQMessageListener(
        consumerGroup = "${spring.application.name}_" + TestDataUtil.TEST_CLASS_TOPIC + "-test-new-consumer",
        topic = TestDataUtil.TEST_CLASS_TOPIC
)
public class TestNewMessageClassPojoConsumer extends AbstractConsumer<MessageClassPojo> {

    @Getter
    private final List<MessageClassPojoWrapper> messageClassPojoWrappers = new CopyOnWriteArrayList<>();

    @Override
    protected void onBaseMessage(BaseMessage<MessageClassPojo> baseMessage) {
        MessageClassPojo MessageClassPojo = baseMessage.getData();
        messageClassPojoWrappers.forEach(MessageClassPojoWrapper -> {
            if (MessageClassPojoWrapper.getMessageClassPojo().equals(MessageClassPojo)) {
                log.info("Received new message: {}", MessageClassPojo.getTimestamp());
                MessageClassPojoWrapper.getCountDownLatch().countDown();
            }
        });
    }
}