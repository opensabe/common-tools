package io.github.opensabe.common.redisson.test.common;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

/**
 * 注意使用这个类的单元测试，用的是同一个 redis，不同单元测试注意隔离不同的 key
 */
@Log4j2
public class SingleRedisIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    public static final int REDIS_PORT = 6379;
    public static final int PORT = 6379;
    public static final GenericContainer REDIS = new FixedHostPortGenericContainer("redis")
            .withExposedPorts(REDIS_PORT)
            .withCommand("redis-server");

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        //由于单元测试并发执行，这个只能启动一次，所以加锁
        if (!REDIS.isRunning()) {
            synchronized (SingleRedisIntegrationTest.class) {
                if (!REDIS.isRunning()) {
                    REDIS.start();
                    log.info("Redis started at port: {}", REDIS.getMappedPort(REDIS_PORT));
                }
            }
        }
    }

    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(REDIS_PORT));
    }

    @Override
    public void close() throws Throwable {
        REDIS.stop();
    }
}
