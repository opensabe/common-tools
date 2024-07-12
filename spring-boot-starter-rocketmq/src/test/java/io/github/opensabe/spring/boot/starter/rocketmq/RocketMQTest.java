package io.github.opensabe.spring.boot.starter.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;
import io.github.opensabe.spring.boot.starter.rocketmq.AbstractMQConsumer;
import io.github.opensabe.spring.boot.starter.rocketmq.MQProducer;
import io.github.opensabe.spring.boot.starter.rocketmq.MQSendConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

//目前镜像只能在 mac 上运行，所以先跳过
@Disabled
@SpringBootTest(properties = {
        //spring-boot 2.6.x 开始，禁止循环依赖（A -> B, B -> A），字段注入一般会导致这种循环依赖，但是我们字段注入太多了，挨个检查太多了
        "spring.main.allow-circular-references=true",
        "appId=test",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "mapper.mappers[0]=io.github.opensabe.common.mybatis.base.BaseMapper",
        "mybatis.configuration.map-underscore-to-camel-case=true",
        "pagehelper.offset-as-page-num=true",
        "pagehelper.support-methods-arguments=true",
        "country.map.0=public",
        "defaultOperId=0",
        "jdbc.config.common.base-packages[0]=io.github.opensabe.common.config.dal.db.dao",
        "jdbc.config.common.data-source[0].cluster-name=public",
        "jdbc.config.common.data-source[0].driver-class-name=com.mysql.jdbc.Driver",
        "jdbc.config.common.data-source[0].is-write-allowed=true",
        "jdbc.config.common.data-source[0].name=user-1",
        "jdbc.config.common.data-source[0].username=root",
        "jdbc.config.common.data-source[0].password=123456",
        "jdbc.config.common.data-source[0].url=jdbc:mysql://127.0.0.1:3307/sys",
        //使用 sqlite 不支持 prepared statement
        "jdbc.config.common.data-source[0].pool_prepared_statements=false",
        "jdbc.config.common.default-cluster-name=public",
        "jdbc.config.common.transaction-service-packages=io.github.opensabe.common",
        "spring.application.name=rocketmq-test",
        "rocketmq.producer.group=rocketmq-test",
        "rocketmq.name-server=localhost:9876",
})
@AutoConfigureObservability
public class RocketMQTest {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @ClassRule
    @SuppressWarnings({"deprecation", "resource"})
    public static GenericContainer<?> redisServer = new FixedHostPortGenericContainer<>("redis")
            .withFixedExposedPort(6379,6379)
            .withExposedPorts(6379);
    @ClassRule
    @SuppressWarnings({"deprecation", "resource"})
    public static GenericContainer<?> mysql = new FixedHostPortGenericContainer<>("mysql")
            .withFixedExposedPort(3307,3306)
            .withExposedPorts(3306)
            .withEnv("MYSQL_ROOT_PASSWORD", "123456");
    @ClassRule
    @SuppressWarnings({"deprecation", "resource"})
    public static GenericContainer<?> rocketmq = new FixedHostPortGenericContainer<>("maheng157/rocketmq-local:5.1.4")
            .withFixedExposedPort(9876,9876)
            .withFixedExposedPort(10911,10911)
            .withExposedPorts(9876,10911)
            ;
    private static volatile Long timestamp = System.currentTimeMillis();
    private static final CountDownLatch testSendLatch = new CountDownLatch(1);

    private static boolean hasInfo = false;

    private static CountDownLatch latch;

    public static final List<String> SENT_MESSAGES = new ArrayList<>();

    @Autowired
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    @BeforeAll
    public static void setUp() {
        rocketmq.start();
        redisServer.start();
        mysql.start();
    }

    @AfterAll
    public static void tearDown() {
        mysql.stop();
        redisServer.stop();
        rocketmq.stop();
    }

    @EnableAutoConfiguration
    @Configuration
    public static class App {
        @Bean
        public TestConsumer testConsumer() {
            return new TestConsumer();
        }
        @Bean
        public TestSecretProvider testSecretProvider(GlobalSecretManager globalSecretManager) {
            return new TestSecretProvider(globalSecretManager);
        }
    }


    @BeforeEach
    void createTable () {
        rocketMQTemplate.getProducer().setSendMsgTimeout(10000);
        dynamicRoutingDataSource.getResolvedDataSources().values().forEach(dataSource -> {
            try (
                    Connection connection = dataSource.getConnection();
                    Statement statement = connection.createStatement()
            ) {
                String sql = "CREATE TABLE if not exists `t_common_mq_fail_log` (" +
                        "  `id` varchar(40)," +
                        "  `topic` varchar(145)," +
                        "  `hash_key` varchar(145)," +
                        "  `trace_id` varchar(50)," +
                        "  `body` text," +
                        "  `send_config` varchar(245)," +
                        "  `retry_num` int," +
                        "  `send_status` int," +
                        "  `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)," +
                        "  `update_time` timestamp(3) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3)," +
                        "  PRIMARY KEY (`id`)" +
                        ") ";
                statement.execute(sql);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
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
        mqProducer.send("rocketmq-test-topic", POJO.builder().text("今天天气不错" + testSendLatch.toString()).timestamp(timestamp).build(), MQSendConfig.builder()
                //重试3次失败后，存入数据库靠定时任务继续重试
                .persistence(true).build());
        testSendLatch.await();
        assertTrue(hasInfo);
    }@Test

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

        boolean isCompleted = latch.await(30L, TimeUnit.SECONDS);

        Assert.assertTrue(isCompleted);
    }

    @Test
    public void testSend_largePayload_DisableCompression() throws Exception {
        try {
            // compression disabled, expect message is not compressed and send will fail
            mqProducer.send(
                    "rocketmq-test-topic",
                    POJO.builder().text(generateLargeMessage(5 * 1024 * 1024)).timestamp(timestamp).build());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
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
                if (pojo.text.contains(testSendLatch.toString())) {
                    testSendLatch.countDown();
                    hasInfo = pojo.text.contains("今天天气不错");
                }

                if (SENT_MESSAGES.contains(pojo.getText()) && latch != null) {
                    latch.countDown();
                }
            }
            catch (JSONException ex) {
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
