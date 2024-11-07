package io.github.opensabe.common.testcontainers.integration;

import io.github.opensabe.common.testcontainers.CustomizedValkeyContainer;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;

public class SingleValkeyIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    public static final CustomizedValkeyContainer VALKEY = new CustomizedValkeyContainer();

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        //由于单元测试并发执行，这个只能启动一次，所以加锁
        if (!VALKEY.isRunning()) {
            synchronized (SingleValkeyIntegrationTest.class) {
                if (!VALKEY.isRunning()) {
                    VALKEY.start();
                }
            }
        }
    }

    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", VALKEY::getHost);
        registry.add("spring.data.redis.port", VALKEY::getRedisPort);
    }

    @Override
    public void close() throws Throwable {
        VALKEY.stop();
    }
}


