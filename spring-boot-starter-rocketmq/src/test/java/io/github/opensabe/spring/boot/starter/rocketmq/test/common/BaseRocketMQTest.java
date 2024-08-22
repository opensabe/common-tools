package io.github.opensabe.spring.boot.starter.rocketmq.test.common;

import io.github.opensabe.common.testcontainers.integration.SingleRedisIntegrationTest;
import io.github.opensabe.common.testcontainers.integration.SingleRocketMQIntegrationTest;
import io.github.opensabe.common.testcontainers.integration.SingleWriteMySQLIntegrationTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Log4j2
@AutoConfigureObservability
@JfrEventTest
@ExtendWith({
        SpringExtension.class,
        SingleRocketMQIntegrationTest.class,
        SingleRedisIntegrationTest.class,
        SingleWriteMySQLIntegrationTest.class
})
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.application.name=rocketmq-test",
        "rocketmq.producer.group=rocketmq-test",
}, classes = BaseRocketMQTest.App.class)
public abstract class BaseRocketMQTest {
    @SpringBootApplication(scanBasePackages = "io.github.opensabe.spring.boot.starter.rocketmq.test")
    public static class App {
    }
    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleRocketMQIntegrationTest.setProperties(registry);
        SingleRedisIntegrationTest.setProperties(registry);
        SingleWriteMySQLIntegrationTest.setProperties(registry);
    }
}
