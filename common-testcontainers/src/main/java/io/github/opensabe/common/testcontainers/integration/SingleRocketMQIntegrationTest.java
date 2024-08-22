package io.github.opensabe.common.testcontainers.integration;

import io.github.opensabe.common.testcontainers.CustomizedRocketMQContainer;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * 注意使用这个类的单元测试，用的是同一个 MySQL, Redis，不同单元测试注意隔离不同的 key
 */
@Log4j2
public class SingleRocketMQIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    public static final CustomizedRocketMQContainer ROCKET_MQ_CONTAINER = new CustomizedRocketMQContainer();


    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        //由于单元测试并发执行，这个只能启动一次，所以加锁
        if (!ROCKET_MQ_CONTAINER.isRunning()) {
            synchronized (SingleRocketMQIntegrationTest.class) {
                if (!ROCKET_MQ_CONTAINER.isRunning()) {
                    ROCKET_MQ_CONTAINER.start();
                }
            }
        }
    }

    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("rocketmq.name-server", () -> "localhost:" + ROCKET_MQ_CONTAINER.getNamesrvPort());
    }

    @Override
    public void close() throws Throwable {
        ROCKET_MQ_CONTAINER.stop();
    }
}
