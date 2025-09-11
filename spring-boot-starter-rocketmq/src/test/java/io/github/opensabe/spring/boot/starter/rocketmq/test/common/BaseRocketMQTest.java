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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;
import io.github.opensabe.common.testcontainers.integration.SingleRedisIntegrationTest;
import io.github.opensabe.common.testcontainers.integration.SingleRocketMQIntegrationTest;
import io.github.opensabe.common.testcontainers.integration.SingleWriteMySQLIntegrationTest;
import io.github.opensabe.spring.boot.starter.rocketmq.MQProducer;
import io.github.opensabe.spring.boot.starter.rocketmq.MQSendConfig;
import lombok.extern.log4j.Log4j2;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
@AutoConfigureObservability
@JfrEventTest
@ExtendWith({
        SpringExtension.class,
        SingleRocketMQIntegrationTest.class,
        SingleRedisIntegrationTest.class,
        SingleWriteMySQLIntegrationTest.class
})
//必须要这个，否则会导致测试方法之间的消费者状态互相影响，同一个类有多个消费者 bean 实例导致消费异常
@DirtiesContext
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.application.name=rocketmq-test",
        "rocketmq.producer.group=rocketmq-test",
}, classes = BaseRocketMQTest.App.class)
public abstract class BaseRocketMQTest {
    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleRocketMQIntegrationTest.setProperties(registry);
        SingleRedisIntegrationTest.setProperties(registry);
        SingleWriteMySQLIntegrationTest.setProperties(registry);
    }

    @SpringBootApplication
    public static class App {
        @Bean
        public SecretProvider testSecretProvider(GlobalSecretManager globalSecretManager) {
            return new TestSecretProvider(globalSecretManager);
        }

        @Bean
        public TestNewMessageClassPojoConsumer testNewMessageClassPojoConsumer() {
            return new TestNewMessageClassPojoConsumer();
        }

        @Bean
        public TestNewMessageRecordPojoConsumer testNewMessageRecordPojoConsumer() {
            return new TestNewMessageRecordPojoConsumer();
        }

        @Bean
        public TestOldMessageClassPojoConsumer testOldMessageClassPojoConsumer() {
            return new TestOldMessageClassPojoConsumer();
        }

        @Bean
        public TestOldMessageRecordPojoConsumer testOldMessageRecordPojoConsumer() {
            return new TestOldMessageRecordPojoConsumer();
        }
    }

    @Autowired
    protected MQProducer mqProducer;

    @Autowired
    protected TestNewMessageClassPojoConsumer testNewMessageClassPojoConsumer;

    @Autowired
    protected TestNewMessageRecordPojoConsumer testNewMessageRecordPojoConsumer;

    @Autowired
    protected TestOldMessageClassPojoConsumer testOldMessageClassPojoConsumer;

    @Autowired
    protected TestOldMessageRecordPojoConsumer testOldMessageRecordPojoConsumer;

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
                    "testSecretProviderKey", Set.of(TestDataUtil.SECRET_MESSAGE)
            );
        }
    }

    @BeforeEach
    public void setup() {
        // 清空消费者的消息列表
        testNewMessageClassPojoConsumer.getMessageClassPojoWrappers().clear();
        testNewMessageRecordPojoConsumer.getMessageRecordPojoWrappers().clear();
        testOldMessageClassPojoConsumer.getMessageClassPojoWrappers().clear();
        testOldMessageRecordPojoConsumer.getMessageRecordPojoWrappers().clear();
        log.info("Setup completed, cleared consumer message lists.");
    }

    @Test
    @DisplayName("测试消息发送和消费 - 验证消息传递功能")
    public void testSend() throws InterruptedException {
        MessageRecordPojoWrapper normalMessageRecordPojoForOld = TestDataUtil.getNormalMessageRecordPojo();
        MessageClassPojoWrapper normalMessageClassPojoForOld = TestDataUtil.getNormalMessageClassPojo();
        MessageRecordPojoWrapper normalMessageRecordPojoForNew = TestDataUtil.getNormalMessageRecordPojo();
        MessageClassPojoWrapper normalMessageClassPojoForNew = TestDataUtil.getNormalMessageClassPojo();

        testNewMessageRecordPojoConsumer.getMessageRecordPojoWrappers().add(normalMessageRecordPojoForNew);
        testNewMessageClassPojoConsumer.getMessageClassPojoWrappers().add(normalMessageClassPojoForNew);
        testOldMessageRecordPojoConsumer.getMessageRecordPojoWrappers().add(normalMessageRecordPojoForOld);
        testOldMessageClassPojoConsumer.getMessageClassPojoWrappers().add(normalMessageClassPojoForOld);

        mqProducer.send(TestDataUtil.TEST_RECORD_TOPIC, normalMessageRecordPojoForNew.getMessageRecordPojo());
        mqProducer.send(TestDataUtil.TEST_RECORD_TOPIC, normalMessageRecordPojoForOld.getMessageRecordPojo());
        mqProducer.send(TestDataUtil.TEST_CLASS_TOPIC, normalMessageClassPojoForNew.getMessageClassPojo());
        mqProducer.send(TestDataUtil.TEST_CLASS_TOPIC, normalMessageClassPojoForOld.getMessageClassPojo());

        // 等待消息被消费
        boolean awaitRecordNew = normalMessageRecordPojoForNew.getCountDownLatch().await(1, TimeUnit.MINUTES);
        boolean awaitRecordOld = normalMessageRecordPojoForOld.getCountDownLatch().await(1, TimeUnit.MINUTES);
        boolean awaitClassNew = normalMessageClassPojoForNew.getCountDownLatch().await(1, TimeUnit.MINUTES);
        boolean awaitClassOld = normalMessageClassPojoForOld.getCountDownLatch().await(1, TimeUnit.MINUTES);

        assertTrue(awaitRecordNew);
        assertTrue(awaitRecordOld);
        assertTrue(awaitClassNew);
        assertTrue(awaitClassOld);
    }

    @Test
    @DisplayName("测试敏感信息过滤 - 验证敏感信息被拒绝发送")
    public void testSendSecret() {
        MessageRecordPojoWrapper normalMessageRecordPojoForOld = TestDataUtil.getSecretMessageRecordPojo();
        MessageClassPojoWrapper normalMessageClassPojoForOld = TestDataUtil.getSecretMessageClassPojo();
        MessageRecordPojoWrapper normalMessageRecordPojoForNew = TestDataUtil.getSecretMessageRecordPojo();
        MessageClassPojoWrapper normalMessageClassPojoForNew = TestDataUtil.getSecretMessageClassPojo();

        assertThrows(RuntimeException.class, () -> {
            mqProducer.send(TestDataUtil.TEST_RECORD_TOPIC, normalMessageRecordPojoForNew.getMessageRecordPojo());
        });
        assertThrows(RuntimeException.class, () -> {
            mqProducer.send(TestDataUtil.TEST_RECORD_TOPIC, normalMessageRecordPojoForOld.getMessageRecordPojo());
        });
        assertThrows(RuntimeException.class, () -> {
            mqProducer.send(TestDataUtil.TEST_CLASS_TOPIC, normalMessageClassPojoForNew.getMessageClassPojo());
        });
        assertThrows(RuntimeException.class, () -> {
            mqProducer.send(TestDataUtil.TEST_CLASS_TOPIC, normalMessageClassPojoForOld.getMessageClassPojo());
        });
    }

    @Test
    @DisplayName("测试大负载消息发送 - 验证压缩和消息大小限制")
    public void testSend_largePayload() throws Exception {
        MessageRecordPojoWrapper normalMessageRecordPojoForOld = TestDataUtil.getLargeMessageRecordPojo();
        MessageClassPojoWrapper normalMessageClassPojoForOld = TestDataUtil.getLargeMessageClassPojo();
        MessageRecordPojoWrapper normalMessageRecordPojoForNew = TestDataUtil.getLargeMessageRecordPojo();
        MessageClassPojoWrapper normalMessageClassPojoForNew = TestDataUtil.getLargeMessageClassPojo();

        testNewMessageRecordPojoConsumer.getMessageRecordPojoWrappers().add(normalMessageRecordPojoForNew);
        testNewMessageClassPojoConsumer.getMessageClassPojoWrappers().add(normalMessageClassPojoForNew);
        testOldMessageRecordPojoConsumer.getMessageRecordPojoWrappers().add(normalMessageRecordPojoForOld);
        testOldMessageClassPojoConsumer.getMessageClassPojoWrappers().add(normalMessageClassPojoForOld);

        mqProducer.send(TestDataUtil.TEST_RECORD_TOPIC, normalMessageRecordPojoForNew.getMessageRecordPojo(), MQSendConfig.builder().isCompressEnabled(true).build());
        mqProducer.send(TestDataUtil.TEST_RECORD_TOPIC, normalMessageRecordPojoForOld.getMessageRecordPojo(), MQSendConfig.builder().isCompressEnabled(true).build());
        mqProducer.send(TestDataUtil.TEST_CLASS_TOPIC, normalMessageClassPojoForNew.getMessageClassPojo(), MQSendConfig.builder().isCompressEnabled(true).build());
        mqProducer.send(TestDataUtil.TEST_CLASS_TOPIC, normalMessageClassPojoForOld.getMessageClassPojo(), MQSendConfig.builder().isCompressEnabled(true).build());

        // 等待消息被消费
        boolean awaitRecordNew = normalMessageRecordPojoForNew.getCountDownLatch().await(1, TimeUnit.MINUTES);
        boolean awaitRecordOld = normalMessageRecordPojoForOld.getCountDownLatch().await(1, TimeUnit.MINUTES);
        boolean awaitClassNew = normalMessageClassPojoForNew.getCountDownLatch().await(1, TimeUnit.MINUTES);
        boolean awaitClassOld = normalMessageClassPojoForOld.getCountDownLatch().await(1, TimeUnit.MINUTES);

        assertTrue(awaitRecordNew);
        assertTrue(awaitRecordOld);
        assertTrue(awaitClassNew);
        assertTrue(awaitClassOld);
    }
}
