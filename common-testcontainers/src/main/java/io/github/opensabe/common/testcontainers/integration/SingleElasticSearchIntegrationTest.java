package io.github.opensabe.common.testcontainers.integration;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * 注意使用这个类的单元测试，用的是同一个 ES，不同单元测试注意隔离不同的 key
 */
@Log4j2
public class SingleElasticSearchIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    public static ElasticsearchContainer ES = new ElasticsearchContainer("elasticsearch:7.17.8")
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("xpack.security.http.ssl.enabled", "false");

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        //由于单元测试并发执行，这个只能启动一次，所以加锁
        if (!ES.isRunning()) {
            synchronized (SingleElasticSearchIntegrationTest.class) {
                if (!ES.isRunning()) {
                    ES.start();
                }
            }
        }
    }

    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.elasticsearch.addresses", () -> {
            String httpHostAddress = ES.getHttpHostAddress();
            return httpHostAddress;
        });
    }

    @Override
    public void close() throws Throwable {
        ES.stop();
    }
}
