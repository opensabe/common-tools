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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.alibaba.fastjson.JSONException;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.entity.base.vo.BaseMessage;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.boot.starter.rocketmq.test.common.BaseRocketMQTest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(RocketMQTest.Config.class)
@DisplayName("RocketMQ消息队列测试")
public class RocketMQTest extends BaseRocketMQTest {

    public static final List<String> SENT_MESSAGES = new ArrayList<>();
    public static final List<String> SENT_LARGE_MESSAGES = new ArrayList<>();
    private static final CountDownLatch testSendLatch = new CountDownLatch(1);
    private static final CountDownLatch testSendLatchOld = new CountDownLatch(1);
    private static final String testSendLatchString = testSendLatch.toString();
    private static final String SECRET = "secretString";
    private static volatile Long timestamp = System.currentTimeMillis();
    private static boolean hasInfo = false;
    private static boolean hasInfoOld = false;
    private static CountDownLatch latch;
    private static CountDownLatch latchOld;
    private static CountDownLatch largeLatch;
    private static CountDownLatch largeLatchOld;
    @Autowired
    private MQProducer mqProducer;

    @Test
    @DisplayName("测试消息发送和消费 - 验证消息传递功能")
    public void testSend() throws InterruptedException {
        mqProducer.send("rocketmq-test-topic", POJO.builder().text("今天天气不错" + testSendLatchString).timestamp(timestamp).build(), MQSendConfig.builder()
                //重试3次失败后，存入数据库靠定时任务继续重试
                .persistence(true).build());
        testSendLatch.await(1, TimeUnit.MINUTES);
        testSendLatchOld.await(1, TimeUnit.MINUTES);
        assertTrue(hasInfo);
        assertTrue(hasInfoOld);
    }

    @Test
    @DisplayName("测试敏感信息过滤 - 验证敏感信息被拒绝发送")
    public void testSendSecret() throws InterruptedException {
        assertThrows(RuntimeException.class, () -> {
            mqProducer.send("rocketmq-test-topic", POJO.builder().text(SECRET + "test").timestamp(timestamp).build(), MQSendConfig.builder()
                    .persistence(false).build());
        });
    }

    @Test
    @Disabled
    @DisplayName("测试大负载消息发送 - 验证压缩和消息大小限制")
    public void testSend_largePayload() throws Exception {

        SENT_LARGE_MESSAGES.add("test_large_msg1" + generateLargeMessage(4 * 1024 * 1024 - 18)); // 4MB message);
        SENT_LARGE_MESSAGES.add("test_large_msg2" + generateLargeMessage(4 * 1024 * 1024 - 1025));
        SENT_LARGE_MESSAGES.add("test_large_msg3" + generateLargeMessage(5 * 1024 * 1024));
        SENT_LARGE_MESSAGES.add("test_large_msg4" + generateLargeMessage(4 * 1024 * 1024 - 500));

        largeLatch = new CountDownLatch(SENT_MESSAGES.size());
        largeLatchOld = new CountDownLatch(SENT_MESSAGES.size());

        SENT_LARGE_MESSAGES.forEach(msg ->
                mqProducer.send(
                        "rocketmq-test-topic",
                        POJO.builder().text(msg).timestamp(timestamp).build(),
                        MQSendConfig.builder().isCompressEnabled(true).build())
        );

        boolean isCompleted = largeLatch.await(1, TimeUnit.MINUTES);

        assertTrue(isCompleted);

        isCompleted = largeLatchOld.await(1, TimeUnit.MINUTES);

        assertTrue(isCompleted);
    }

    private String generateLargeMessage(int size) {
        StringBuilder stringB = new StringBuilder(size);
        String paddingString = "abcdefghijklmnopqrs";

        while (stringB.length() + paddingString.length() < size) {
            stringB.append(paddingString);
        }

        return stringB.toString();
    }

    public static class Config {
        @Bean
        public TestConsumer testConsumer() {
            return new TestConsumer();
        }

        @Bean
        public TestOldConsumer testOldConsumer() {
            return new TestOldConsumer();
        }

        @Bean
        public TestSecretProvider testSecretProvider(GlobalSecretManager globalSecretManager) {
            return new TestSecretProvider(globalSecretManager);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class POJO {
        private Long timestamp;
        private String text;
    }

    @RocketMQMessageListener(
            consumerGroup = "${spring.application.name}_rocketmq-test-topic",
            topic = "rocketmq-test-topic"
    )
    public static class TestConsumer extends AbstractConsumer<POJO> {


        @Override
        protected void onBaseMessage(BaseMessage<POJO> baseMessage) {
            try {
                POJO pojo = baseMessage.getData();
                if (pojo.text.contains(testSendLatchString)) {
                    hasInfo = pojo.text.contains("今天天气不错");
                    testSendLatch.countDown();
                }

                if (SENT_MESSAGES.contains(pojo.getText()) && latch != null) {
                    latch.countDown();
                }

                if (SENT_LARGE_MESSAGES.contains(pojo.getText()) && largeLatch != null) {
                    largeLatch.countDown();
                }
            } catch (JSONException ex) {
                System.out.println("failed parse object: " + ex.getMessage() + ": " + baseMessage.getData());
            }
        }
    }

    @RocketMQMessageListener(
            consumerGroup = "${spring.application.name}_rocketmq-test-topic-old",
            topic = "rocketmq-test-topic"
    )
    public static class TestOldConsumer extends AbstractMQConsumer {

        @Override
        protected void onBaseMQMessage(BaseMQMessage baseMQMessage) {
            try {
                POJO pojo = JsonUtil.parseObject(baseMQMessage.getData(), POJO.class);
                if (pojo.text.contains(testSendLatchString)) {
                    hasInfoOld = pojo.text.contains("今天天气不错");
                    testSendLatchOld.countDown();
                }

                if (SENT_MESSAGES.contains(pojo.getText()) && latchOld != null) {
                    latchOld.countDown();
                }

                if (SENT_LARGE_MESSAGES.contains(pojo.getText()) && largeLatch != null) {
                    largeLatchOld.countDown();
                }
            } catch (JSONException ex) {
                System.out.println("failed parse object: " + ex.getMessage() + ": " + baseMQMessage.getData());
            }
        }
    }

    public static class TestSecretProvider extends SecretProvider {
        protected TestSecretProvider(GlobalSecretManager globalSecretManager) {
            super(globalSecretManager);
        }

        @Override
        protected String name() {
            return "testSecretProvider";
        }

        @Override
        protected long reloadTimeInterval() {
            return 1;
        }

        @Override
        protected TimeUnit reloadTimeIntervalUnit() {
            return TimeUnit.DAYS;
        }

        @Override
        protected Map<String, Set<String>> reload() {
            return Map.of(
                    "testSecretProviderKey", Set.of(SECRET)
            );
        }
    }
}
