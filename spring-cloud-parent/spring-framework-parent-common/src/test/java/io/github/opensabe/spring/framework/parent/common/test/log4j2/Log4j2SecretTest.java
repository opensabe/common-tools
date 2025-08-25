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
package io.github.opensabe.spring.framework.parent.common.test.log4j2;

import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;
import io.github.opensabe.common.utils.SpringUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
@SpringBootTest(
        classes = Log4j2SecretTest.Main.class
)
@AutoConfigureObservability
@DisplayName("Log4j2敏感信息过滤测试")
public class Log4j2SecretTest {
    private static final String SECRET = "secretString-Log4j2SecretTest";
    private static final String IDENTIFIER = "IDENTIFIER-Log4j2SecretTest";

    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    private static final AtomicBoolean hasSecret = new AtomicBoolean(false);

    @SpringBootApplication
    public static class Main {
        @Bean
        public TestSecretProvider testSecretProvider(GlobalSecretManager globalSecretManager) {
            return new TestSecretProvider(globalSecretManager);
        }
    }

    @Test
    /**
     * 测试 Log4j2 日志中是否包含敏感信息
     * 在并发单元测试的时候需要禁止执行，因为用到了 SpringUtil 获取 GlobalSecretManager
     * 多线程测试时，单元测试之间的 SpringUtil 的 ApplicationContext 可能会被覆盖污染
     * 因此需要在单元测试中禁用
     * 在开发的时候单独执行
     */
    @Disabled
    @DisplayName("测试Log4j2敏感信息过滤 - 验证敏感信息不被记录到日志")
    public void testLog4j2() throws InterruptedException {
        log.info("{} test {} test {} test {}", IDENTIFIER, SECRET, SECRET + SECRET, SECRET + "xx");
        countDownLatch.await();
        //不能有 secret 字符串
        Assertions.assertFalse(hasSecret.get());
    }

    public static class TestSecretProvider extends SecretProvider {
        protected TestSecretProvider(GlobalSecretManager globalSecretManager) {
            super(globalSecretManager);
        }

        @Override
        protected String name() {
            return "testSecretProvider";
        }

        @Override
        protected long reloadTimeInterval() {
            return 1;
        }

        @Override
        protected TimeUnit reloadTimeIntervalUnit() {
            return TimeUnit.DAYS;
        }

        @Override
        protected Map<String, Set<String>> reload() {
            return Map.of(
                    "testSecretProviderKey", Set.of(SECRET)
            );
        }
    }

    @Plugin(name = "CustomAppender", category = "Core", elementType = Appender.ELEMENT_TYPE, printObject = true)
    public static class CustomAppender extends AbstractAppender {

        protected CustomAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
            super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
        }

        @PluginFactory
        public static CustomAppender createAppender(
                @PluginAttribute("name") String name,
                @PluginElement("Filter") Filter filter,
                @PluginElement("Layout") Layout<? extends Serializable> layout,
                @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
            return new CustomAppender(name, filter, layout, ignoreExceptions);
        }

        @Override
        public void append(LogEvent event) {
            String formattedMessage = event.getMessage().getFormattedMessage();
            System.out.println(formattedMessage);
            if (formattedMessage.contains(SECRET)) {
                System.out.println("Found secret in log: " + formattedMessage);
                hasSecret.set(true);
            }
            if (formattedMessage.contains(IDENTIFIER)) {
                System.out.println("Found identifier in log: " + formattedMessage);
                countDownLatch.countDown();
            }
        }
    }
}
