package io.github.opensabe.spring.boot.starter.rocketmq.test.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import io.github.opensabe.common.entity.base.vo.BaseMessage;
import io.github.opensabe.spring.boot.starter.rocketmq.AbstractConsumer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RocketMQMessageListener(
        consumerGroup = "${spring.application.name}_" + TestDataUtil.TEST_RECORD_TOPIC + "-test-new-consumer",
        topic = TestDataUtil.TEST_RECORD_TOPIC
)
public class TestNewMessageRecordPojoConsumer extends AbstractConsumer<MessageRecordPojo> {

    @Getter
    private final List<MessageRecordPojoWrapper> messageRecordPojoWrappers = new CopyOnWriteArrayList<>();

    @Override
    protected void onBaseMessage(BaseMessage<MessageRecordPojo> baseMessage) {
        MessageRecordPojo messageRecordPojo = baseMessage.getData();
        messageRecordPojoWrappers.forEach(messageRecordPojoWrapper -> {
            if (messageRecordPojoWrapper.getMessageRecordPojo().equals(messageRecordPojo)) {
                log.info("Received new message: {}", messageRecordPojo.timestamp());
                messageRecordPojoWrapper.getCountDownLatch().countDown();
            }
        });
    }
}