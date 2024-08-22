package io.github.opensabe.common.idgenerator.test.common;

import io.github.opensabe.common.testcontainers.integration.SingleRedisIntegrationTest;
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
@JfrEventTest
@AutoConfigureObservability
@ExtendWith({SpringExtension.class, SingleRedisIntegrationTest.class})
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
}, classes = BaseUniqueIdTest.App.class)
public abstract class BaseUniqueIdTest {
    @SpringBootApplication(scanBasePackages = {"io.github.opensabe.common.idgenerator.test"})
    public static class App {
    }
    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleRedisIntegrationTest.setProperties(registry);
    }
}
