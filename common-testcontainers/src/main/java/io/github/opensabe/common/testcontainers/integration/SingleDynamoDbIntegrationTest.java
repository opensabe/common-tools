package io.github.opensabe.common.testcontainers.integration;

import io.github.opensabe.common.testcontainers.CustomizedDynamoDBContainer;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * 注意使用这个类的单元测试，用的是同一个 dynamodb，不同单元测试注意隔离不同的 key
 */
@Log4j2
public class SingleDynamoDbIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    public static CustomizedDynamoDBContainer DYNAMO_DB = new CustomizedDynamoDBContainer();

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        //由于单元测试并发执行，这个只能启动一次，所以加锁
        if (!DYNAMO_DB.isRunning()) {
            synchronized (SingleDynamoDbIntegrationTest.class) {
                if (!DYNAMO_DB.isRunning()) {
                    DYNAMO_DB.start();
                }
            }
        }
    }

    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("aws_access_key_id", () -> "fake");
        registry.add("aws_secret_access_key", () -> "fake");
        registry.add("aws_env", () -> "test");
        registry.add("dynamolLocalUrl", () -> "http://localhost:" + DYNAMO_DB.getDynamoDBPort());
        registry.add("defaultOperId", () -> 0);
    }

    @Override
    public void close() throws Throwable {
        DYNAMO_DB.stop();
    }
}
