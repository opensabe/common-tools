package io.github.opensabe.spring.boot.starter.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;
import io.github.opensabe.spring.boot.starter.rocketmq.test.common.BaseRocketMQTest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(RocketMQTest.Config.class)
public class RocketMQTest extends BaseRocketMQTest {

    private static volatile Long timestamp = System.currentTimeMillis();
    private static final CountDownLatch testSendLatch = new CountDownLatch(1);
    private static final String testSendLatchString = testSendLatch.toString();
    private static boolean hasInfo = false;
    private static CountDownLatch latch;
    public static final List<String> SENT_MESSAGES = new ArrayList<>();

    public static class Config {
        @Bean
        public TestConsumer testConsumer() {
            return new TestConsumer();
        }

        @Bean
        public TestSecretProvider testSecretProvider(GlobalSecretManager globalSecretManager) {
            return new TestSecretProvider(globalSecretManager);
        }
    }

    @Autowired
    private MQProducer mqProducer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class POJO {
        private Long timestamp;
        private String text;
    }

    @Test
    public void testSend() throws InterruptedException {
        mqProducer.send("rocketmq-test-topic", POJO.builder().text("今天天气不错" + testSendLatchString).timestamp(timestamp).build(), MQSendConfig.builder()
                //重试3次失败后，存入数据库靠定时任务继续重试
                .persistence(true).build());
        testSendLatch.await(1, TimeUnit.MINUTES);
        assertTrue(hasInfo);
    }

    @Test
    public void testSendSecret() throws InterruptedException {
        assertThrows(RuntimeException.class, () -> {
            mqProducer.send("rocketmq-test-topic", POJO.builder().text(SECRET + "test").timestamp(timestamp).build(), MQSendConfig.builder()
                    .persistence(false).build());
        });
    }

    @Test
    public void testSend_largePayload() throws Exception {

        SENT_MESSAGES.add("test_msg1" + generateLargeMessage(4 * 1024 * 1024 - 18)); // 4MB message);
        SENT_MESSAGES.add("test_msg2" + generateLargeMessage(4 * 1024 * 1024 - 1025));
        SENT_MESSAGES.add("test_msg3" + generateLargeMessage(5 * 1024 * 1024));
        SENT_MESSAGES.add("test_msg4" + generateLargeMessage(4 * 1024 * 1024 - 500));

        latch = new CountDownLatch(SENT_MESSAGES.size());

        SENT_MESSAGES.forEach(msg ->
                mqProducer.send(
                        "rocketmq-test-topic",
                        POJO.builder().text(msg).timestamp(timestamp).build(),
                        MQSendConfig.builder().isCompressEnabled(true).build())
        );

        boolean isCompleted = latch.await(1, TimeUnit.MINUTES);

        Assert.assertTrue(isCompleted);
    }

    private String generateLargeMessage(int size) {
        StringBuilder stringB = new StringBuilder(size);
        String paddingString = "abcdefghijklmnopqrs";

        while (stringB.length() + paddingString.length() < size)
            stringB.append(paddingString);

        return stringB.toString();
    }

    @RocketMQMessageListener(
            consumerGroup = "${spring.application.name}_rocketmq-test-topic",
            topic = "rocketmq-test-topic"
    )
    public static class TestConsumer extends AbstractMQConsumer {

        @Override
        protected void onBaseMQMessage(BaseMQMessage baseMQMessage) {
            try {
                POJO pojo = JSON.parseObject(baseMQMessage.getData(), POJO.class);
                if (pojo.text.contains(testSendLatchString)) {
                    hasInfo = pojo.text.contains("今天天气不错");
                    testSendLatch.countDown();
                }

                if (SENT_MESSAGES.contains(pojo.getText()) && latch != null) {
                    latch.countDown();
                }
            } catch (JSONException ex) {
                System.out.println("failed parse object: " + ex.getMessage() + ": " + baseMQMessage.getData());
            }
        }
    }

    private static final String SECRET = "secretString";

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
