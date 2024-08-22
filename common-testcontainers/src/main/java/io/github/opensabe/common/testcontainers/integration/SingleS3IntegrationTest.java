package io.github.opensabe.common.testcontainers.integration;

import io.github.opensabe.common.testcontainers.CustomizedS3Container;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * 注意使用这个类的单元测试，用的是同一个 S3，不同单元测试注意隔离不同的 key
 */
@Log4j2
public class SingleS3IntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    public static CustomizedS3Container AWS_S3 = new CustomizedS3Container();

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        //由于单元测试并发执行，这个只能启动一次，所以加锁
        if (!AWS_S3.isRunning()) {
            synchronized (SingleS3IntegrationTest.class) {
                if (!AWS_S3.isRunning()) {
                    AWS_S3.start();
                }
            }
        }
    }

    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.s3.enabled", () -> true);
        registry.add("aws.s3.accessKeyId", () -> "fake");
        registry.add("aws.s3.accessKey", () -> "fake");
        registry.add("aws.s3.region", () -> "us-east-1");
        registry.add("awsS3LocalUrl", () -> "http://s3.localhost.localstack.cloud:" + AWS_S3.getS3Port());
    }

    @Override
    public void close() throws Throwable {
        AWS_S3.stop();
    }
}
