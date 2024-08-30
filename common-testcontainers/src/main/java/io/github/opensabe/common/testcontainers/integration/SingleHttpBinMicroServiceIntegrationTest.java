package io.github.opensabe.common.testcontainers.integration;

import io.github.opensabe.common.testcontainers.CustomizedHttpBinContainer;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * 注册一个微服务
 */
@Log4j2
public class SingleHttpBinMicroServiceIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    public static CustomizedHttpBinContainer HTTP_BIN = new CustomizedHttpBinContainer();
    public static final String HTTP_BIN_SERVICE_NAME = "httpbin";
    public static final String HTTP_BIN_SERVICE_ZONE = "test";
    public static final String HTTP_BIN_SERVICE_INSTANCE_ID = "test-httpbin-1";

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        //由于单元测试并发执行，这个只能启动一次，所以加锁
        if (!HTTP_BIN.isRunning()) {
            synchronized (SingleHttpBinMicroServiceIntegrationTest.class) {
                if (!HTTP_BIN.isRunning()) {
                    HTTP_BIN.start();
                }
            }
        }
    }

    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.cloud.discovery.client.simple.instances." + HTTP_BIN_SERVICE_NAME + "[0].uri",
                () -> "http://localhost:" + HTTP_BIN.getHttpBinPort()
        );
        registry.add("spring.cloud.discovery.client.simple.instances.httpbin[0].metadata.zone", () -> {
            return HTTP_BIN_SERVICE_ZONE;
        });
        registry.add("spring.cloud.discovery.client.simple.instances.httpbin[0].instance-id", () -> {
            return HTTP_BIN_SERVICE_INSTANCE_ID;
        });
    }

    @Override
    public void close() throws Throwable {
        HTTP_BIN.stop();
    }
}
