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
package io.github.opensabe.spring.boot.starter.rocketmq.test.common;

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
        consumerGroup = "${spring.application.name}_" + TestDataUtil.TEST_CLASS_TOPIC + "-test-old-consumer",
        topic = TestDataUtil.TEST_CLASS_TOPIC
)
public class TestOldMessageClassPojoConsumer extends AbstractMQConsumer {

    @Getter
    private final List<MessageClassPojoWrapper> messageClassPojoWrappers = new CopyOnWriteArrayList<>();

    @Override
    protected void onBaseMQMessage(BaseMQMessage baseMQMessage) {
        String data = baseMQMessage.getData();
        MessageClassPojo messageClassPojo = JsonUtil.parseObject(data, MessageClassPojo.class);
        messageClassPojoWrappers.forEach(MessageClassPojoWrapper -> {
            if (MessageClassPojoWrapper.getMessageClassPojo().equals(messageClassPojo)) {
                log.info("Received old message: {}", messageClassPojo.getTimestamp());
                MessageClassPojoWrapper.getCountDownLatch().countDown();
            }
        });
    }
}