package io.github.opensabe.spring.cloud.parent.common.test.config;

import io.github.opensabe.spring.cloud.parent.common.config.OnlyOnceApplicationListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = OnlyOnceApplicationListenerTest.TestConfig.class
)
class OnlyOnceApplicationListenerTest {

    private static final AtomicInteger executionCount = new AtomicInteger(0);
    @BeforeEach
    void setUp() {
        // 重置计数器
        executionCount.set(0);
    }

    @SpringBootApplication
    static class TestConfig {

        @Bean
        public TestOnlyOnceListener testOnlyOnceListener() {
            return new TestOnlyOnceListener();
        }
    }

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

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