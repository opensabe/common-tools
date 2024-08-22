package io.github.opensabe.common.redisson.test.common;

import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, SingleRedisIntegrationTest.class})
@AutoConfigureObservability
@SpringBootTest(
        classes = BaseRedissonTest.App.class,
        properties = {
                "eureka.client.enabled=false",
        }
)
@JfrEventTest
public abstract class BaseRedissonTest {
    @SpringBootApplication
    public static class App {
    }

    public static final int AOP_ORDER = 10000;

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleRedisIntegrationTest.setProperties(registry);
        registry.add("spring.redis.redisson.aop.order", () -> AOP_ORDER);
    }
}
