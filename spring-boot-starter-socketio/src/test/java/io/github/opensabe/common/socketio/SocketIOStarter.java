package io.github.opensabe.common.socketio;

import io.github.opensabe.common.testcontainers.integration.SingleRedisIntegrationTest;
import io.github.opensabe.common.testcontainers.integration.SingleRocketMQIntegrationTest;
import io.github.opensabe.common.testcontainers.integration.SingleWriteMySQLIntegrationTest;
import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

@Log4j2
@JfrEventTest
@AutoConfigureObservability
@SpringBootTest(properties = {
        "rocketmq.producer.group=socketio",
}, classes = SocketIOStarter.App.class)
@ExtendWith({
        SingleRocketMQIntegrationTest.class,
        SingleRedisIntegrationTest.class,
        SingleWriteMySQLIntegrationTest.class,
})
public class SocketIOStarter {
    @SpringBootApplication(scanBasePackages = {"io.github.opensabe.spring.boot.starter.socketio"})
    public static class App {
        public static void main(String[] args) {
            SpringApplication.run(App.class, args);
        }
    }
    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleRedisIntegrationTest.setProperties(registry);
        SingleRocketMQIntegrationTest.setProperties(registry);
        SingleWriteMySQLIntegrationTest.setProperties(registry);
    }

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @BeforeEach
    void createTable() {
        rocketMQTemplate.getProducer().setSendMsgTimeout(10000);
    }

    @AfterEach
    void dropTable () {
    }
}
