package io.github.opensabe.common.socketio;

import io.github.opensabe.common.testcontainers.integration.SingleRedisIntegrationTest;
import io.github.opensabe.spring.boot.starter.rocketmq.autoconf.RocketMQAutoConfiguration;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Log4j2
@JfrEventTest
@AutoConfigureObservability
@SpringBootTest(classes = SocketIOStarter.App.class)
@ExtendWith(SingleRedisIntegrationTest.class)
public class SocketIOStarter {
    @SpringBootApplication(scanBasePackages = "io.github.opensabe.spring.boot.starter.socketio",
            exclude = RocketMQAutoConfiguration.class)
    public static class App {
        public static void main(String[] args) {
            SpringApplication.run(App.class, args);
        }
    }
    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleRedisIntegrationTest.setProperties(registry);
    }
}
