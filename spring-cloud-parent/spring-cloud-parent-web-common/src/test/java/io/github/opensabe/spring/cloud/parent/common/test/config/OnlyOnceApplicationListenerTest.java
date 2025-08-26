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
package io.github.opensabe.spring.cloud.parent.common.test.config;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

import io.github.opensabe.spring.cloud.parent.common.config.OnlyOnceApplicationListener;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = OnlyOnceApplicationListenerTest.TestConfig.class
)
class OnlyOnceApplicationListenerTest {

    private static final AtomicInteger executionCount = new AtomicInteger(0);
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        // 重置计数器
        executionCount.set(0);
    }

    /**
     * 测试继承 OnlyOnceApplicationListener 的 ApplicationReadyEvent 监听器只执行一次
     */
    @Test
    void testOnlyOnceApplicationReadyEvent() {
        // 发布 TestOnlyOnceSpringApplicationEvent 事件两次
        applicationEventPublisher.publishEvent(new TestOnlyOnceSpringApplicationEvent(
                new SpringApplication(), new String[0])
        );
        applicationEventPublisher.publishEvent(new TestOnlyOnceSpringApplicationEvent(
                new SpringApplication(), new String[0])
        );
        // 验证只执行了一次
        assertEquals(1, executionCount.get(), "ApplicationReadyEvent 监听器应该只执行一次");
    }

    @SpringBootApplication
    static class TestConfig {

        @Bean
        public TestOnlyOnceListener testOnlyOnceListener() {
            return new TestOnlyOnceListener();
        }
    }

    /**
     * 特殊的 SpringApplicationEvent 用于测试 OnlyOnceApplicationListener
     */
    static class TestOnlyOnceSpringApplicationEvent extends SpringApplicationEvent {
        public TestOnlyOnceSpringApplicationEvent(SpringApplication application, String[] args) {
            super(application, args);
        }
    }

    /**
     * 测试用的 TestOnlyOnceSpringApplicationEvent 监听器
     */
    static class TestOnlyOnceListener extends OnlyOnceApplicationListener<TestOnlyOnceSpringApplicationEvent> {

        @Override
        protected void onlyOnce(TestOnlyOnceSpringApplicationEvent event) {
            executionCount.incrementAndGet();
        }
    }
} 