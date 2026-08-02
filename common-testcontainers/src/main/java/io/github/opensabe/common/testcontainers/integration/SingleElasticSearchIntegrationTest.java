/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.testcontainers.integration;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import lombok.extern.log4j.Log4j2;

/**
 * 单例 Elasticsearch Testcontainers 集成基类。
 * <p>
 * 使用本类的测试共享同一个 ES 容器实例；不同测试之间请注意索引或数据 key 的隔离。
 * 容器镜像与 Boot 4.1 / elasticsearch-java 9.x（Rest5 客户端）对齐。
 */
@Log4j2
public class SingleElasticSearchIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    /**
     * 全局共享的 Elasticsearch 容器，单节点模式且关闭安全认证。
     */
    public static final ElasticsearchContainer ES = new ElasticsearchContainer("elasticsearch:9.1.5")
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("xpack.security.http.ssl.enabled", "false")
            .withEnv("xpack.security.transport.ssl.enabled", "false");

    /**
     * 向 Spring 测试上下文注册 ES 连接地址属性。
     *
     * @param registry 动态属性注册器
     */
    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.elasticsearch.addresses", () -> {
            String httpHostAddress = ES.getHttpHostAddress();
            return httpHostAddress;
        });
    }

    /**
     * 在所有测试启动前确保 ES 容器已运行；并发测试场景下通过类锁保证只启动一次。
     *
     * @param extensionContext JUnit 扩展上下文
     * @throws Exception 容器启动失败时抛出
     */
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        if (!ES.isRunning()) {
            synchronized (SingleElasticSearchIntegrationTest.class) {
                if (!ES.isRunning()) {
                    ES.start();
                }
            }
        }
    }

    /**
     * 关闭并释放共享 ES 容器资源。
     *
     * @throws Throwable 停止容器失败时抛出
     */
    @Override
    public void close() throws Throwable {
        ES.stop();
    }
}
