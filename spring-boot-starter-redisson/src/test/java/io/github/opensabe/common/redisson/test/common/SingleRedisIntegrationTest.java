package io.github.opensabe.common.redisson.test.common;

import io.github.opensabe.common.testcontainers.CustomizedRedisContainer;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.TimeUnit;

/**
 * 注意使用这个类的单元测试，用的是同一个 redis，不同单元测试注意隔离不同的 key
 */
@Log4j2
public class SingleRedisIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    public static final CustomizedRedisContainer REDIS = new CustomizedRedisContainer();

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        //由于单元测试并发执行，这个只能启动一次，所以加锁
        if (!REDIS.isRunning()) {
            synchronized (SingleRedisIntegrationTest.class) {
                if (!REDIS.isRunning()) {
                    REDIS.start();
                }
            }
        }
    }

    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", REDIS::getRedisPort);
    }

    @Override
    public void close() throws Throwable {
        REDIS.stop();
    }
}
