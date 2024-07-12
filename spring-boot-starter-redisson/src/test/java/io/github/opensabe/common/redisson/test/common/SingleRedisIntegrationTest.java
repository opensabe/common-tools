package io.github.opensabe.common.redisson.test.common;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

/**
 * 注意使用这个类的单元测试，用的是同一个 redis，不同单元测试注意隔离不同的 key
 */
public class SingleRedisIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    public static final int PORT = 16379;
    public static final GenericContainer REDIS = new FixedHostPortGenericContainer("redis")
            //使用不常用的 port 防止冲突
            .withFixedExposedPort(PORT,6379)
            .withExposedPorts(6379)
            .withCommand("redis-server");

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
        System.out.println("redis started");
    }

    @Override
    public void close() throws Throwable {
        REDIS.stop();
        System.out.println("redis stopped");
    }
}
