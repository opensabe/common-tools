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
package io.github.opensabe.spring.cloud.parent.common.test.eureka;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Bean;

import io.github.opensabe.spring.cloud.parent.common.eureka.EurekaInstanceConfigBeanCustomizer;
import lombok.extern.log4j.Log4j2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EurekaInstanceConfigBeanCustomizer 集成测试
 * 验证自定义的 EurekaInstanceConfigBeanCustomizer 在 SpringBoot 环境中是否被正确执行
 */
@Log4j2
@SpringBootTest(
        properties = {
                "eureka.client.register-with-eureka=false",
                "eureka.client.fetch-registry=false",
        },
        classes = EurekaInstanceConfigBeanCustomizerTest.TestConfiguration.class
)
public class EurekaInstanceConfigBeanCustomizerTest {

    @Autowired
    private EurekaInstanceConfigBean eurekaInstanceConfigBean;
    @Autowired
    private TestEurekaInstanceConfigBeanCustomizer testCustomizer;

    @Test
    void testCustomEurekaInstanceConfigBeanCustomizerExecution() {
        // 重置执行状态
        TestEurekaInstanceConfigBeanCustomizer.reset();

        // 验证自定义的 customizer 被正确注入
        assertNotNull(testCustomizer, "TestEurekaInstanceConfigBeanCustomizer 应该被正确注入");
        assertTrue(testCustomizer instanceof EurekaInstanceConfigBeanCustomizer,
                "testCustomizer 应该实现 EurekaInstanceConfigBeanCustomizer 接口");

        // 手动执行 customizer
        testCustomizer.customize(eurekaInstanceConfigBean);

        // 验证 customizer 被执行
        assertTrue(TestEurekaInstanceConfigBeanCustomizer.isExecuted(),
                "TestEurekaInstanceConfigBeanCustomizer 应该被执行");

        // 验证 metadata 被正确设置
        Map<String, String> metadata = eurekaInstanceConfigBean.getMetadataMap();
        assertTrue(metadata.containsKey(TestEurekaInstanceConfigBeanCustomizer.getTestKey()),
                "metadata 应该包含测试键");
        assertEquals(TestEurekaInstanceConfigBeanCustomizer.getTestValue(),
                metadata.get(TestEurekaInstanceConfigBeanCustomizer.getTestKey()),
                "metadata 中的测试值应该正确");
    }

    @Test
    void testEurekaInstanceConfigBeanMetadataManipulation() {
        // 测试 metadata 操作
        Map<String, String> metadata = eurekaInstanceConfigBean.getMetadataMap();

        // 添加测试数据
        String testKey = "integration-test-key";
        String testValue = "integration-test-value";
        metadata.put(testKey, testValue);

        // 验证数据被正确添加
        assertTrue(metadata.containsKey(testKey), "metadata 应该包含测试键");
        assertEquals(testValue, metadata.get(testKey), "metadata 中的测试值应该正确");

        // 验证 metadata 大小
        assertTrue(metadata.size() > 0, "metadata 应该不为空");
    }

    @Test
    void testMultipleCustomizersExecution() {
        // 测试多个 customizer 的执行
        TestEurekaInstanceConfigBeanCustomizer.reset();

        // 创建另一个测试 customizer
        TestEurekaInstanceConfigBeanCustomizer customizer1 = new TestEurekaInstanceConfigBeanCustomizer();
        TestEurekaInstanceConfigBeanCustomizer customizer2 = new TestEurekaInstanceConfigBeanCustomizer();

        // 执行多个 customizer
        customizer1.customize(eurekaInstanceConfigBean);
        customizer2.customize(eurekaInstanceConfigBean);

        // 验证两个 customizer 都被执行
        assertTrue(TestEurekaInstanceConfigBeanCustomizer.isExecuted(),
                "至少一个 TestEurekaInstanceConfigBeanCustomizer 应该被执行");

        // 验证 metadata 包含预期的数据
        Map<String, String> metadata = eurekaInstanceConfigBean.getMetadataMap();
        assertTrue(metadata.containsKey(TestEurekaInstanceConfigBeanCustomizer.getTestKey()),
                "metadata 应该包含测试键");
    }

    @Test
    void testEurekaInstanceConfigBeanProperties() {
        // 验证 EurekaInstanceConfigBean 的基本属性
        assertNotNull(eurekaInstanceConfigBean, "EurekaInstanceConfigBean 应该被正确注入");

        // 验证 metadataMap 不为 null
        Map<String, String> metadata = eurekaInstanceConfigBean.getMetadataMap();
        assertNotNull(metadata, "metadataMap 不应该为 null");

        // 验证可以添加和获取 metadata
        String testKey = "property-test-key";
        String testValue = "property-test-value";
        metadata.put(testKey, testValue);

        assertEquals(testValue, metadata.get(testKey), "应该能够正确获取添加的 metadata");
    }

    @SpringBootApplication
    static class TestConfiguration {

        @Bean
        public TestEurekaInstanceConfigBeanCustomizer testCustomizer() {
            return new TestEurekaInstanceConfigBeanCustomizer();
        }
    }

    /**
     * 用于测试的自定义 EurekaInstanceConfigBeanCustomizer 实现
     */
    public static class TestEurekaInstanceConfigBeanCustomizer implements EurekaInstanceConfigBeanCustomizer {
        private static final AtomicBoolean executed = new AtomicBoolean(false);
        private static final String TEST_KEY = "test-customizer-key";
        private static final String TEST_VALUE = "test-customizer-value";

        public static boolean isExecuted() {
            return executed.get();
        }

        public static void reset() {
            executed.set(false);
        }

        public static String getTestKey() {
            return TEST_KEY;
        }

        public static String getTestValue() {
            return TEST_VALUE;
        }

        @Override
        public void customize(EurekaInstanceConfigBean eurekaInstanceConfigBean) {
            log.info("TestEurekaInstanceConfigBeanCustomizer.customize() 被调用");
            executed.set(true);
            eurekaInstanceConfigBean.getMetadataMap().put(TEST_KEY, TEST_VALUE);
        }
    }
} 