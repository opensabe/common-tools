package io.github.opensabe.common.secret;

import io.github.opensabe.common.utils.SpringUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class Log4j2SecretCheckFilterTest {
    private AutoCloseable autoCloseable;
    private MockedStatic<SpringUtil> mockedStatic;

    private Log4j2SecretCheckFilter filter;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private GlobalSecretManager globalSecretManager;

    @BeforeEach
    void setUp() {
        mockedStatic = Mockito.mockStatic(SpringUtil.class);
        autoCloseable = MockitoAnnotations.openMocks(this);
        filter = Log4j2SecretCheckFilter.createFilter();
        when(applicationContext.getBean(GlobalSecretManager.class)).thenReturn(globalSecretManager);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
        mockedStatic.close();
    }

    @Test
    void testFilter_WithSensitiveContent() {
        // 准备测试数据
        String sensitiveMessage = "This contains password123";
        Message message = new SimpleMessage(sensitiveMessage);
        LogEvent event = Log4jLogEvent.newBuilder()
                .setMessage(message)
                .build();
        
        FilterSecretStringResult result = FilterSecretStringResult.builder()
                .foundSensitiveString(true)
                .filteredContent("This contains ******")
                .build();

        mockedStatic.when(SpringUtil::getApplicationContext).thenReturn(applicationContext);
        when(globalSecretManager.filterSecretStringAndAlarm(sensitiveMessage)).thenReturn(result);

        // 执行测试
        Log4j2SecretCheckFilter.Result filterResult = filter.filter(event);

        // 验证结果
        assertEquals(Log4j2SecretCheckFilter.Result.DENY, filterResult);
    }

    @Test
    void testFilter_WithNonSensitiveContent() {
        // 准备测试数据
        String normalMessage = "This is a normal message";
        Message message = new SimpleMessage(normalMessage);
        LogEvent event = Log4jLogEvent.newBuilder()
                .setMessage(message)
                .build();
        
        FilterSecretStringResult result = FilterSecretStringResult.builder()
                .foundSensitiveString(false)
                .filteredContent(normalMessage)
                .build();

        mockedStatic.when(SpringUtil::getApplicationContext).thenReturn(applicationContext);
        when(globalSecretManager.filterSecretStringAndAlarm(anyString())).thenReturn(result);

        // 执行测试
        Log4j2SecretCheckFilter.Result filterResult = filter.filter(event);

        // 验证结果
        assertEquals(Log4j2SecretCheckFilter.Result.NEUTRAL, filterResult);
    }

    @Test
    void testFilter_WithNullMessage() {
        // 准备测试数据
        Message message = new SimpleMessage(null);
        LogEvent event = Log4jLogEvent.newBuilder()
                .setMessage(message)
                .build();

        // 执行测试
        Log4j2SecretCheckFilter.Result filterResult = filter.filter(event);

        // 验证结果
        assertEquals(Log4j2SecretCheckFilter.Result.NEUTRAL, filterResult);
    }

    @Test
    void testFilter_WithEmptyMessage() {
        // 准备测试数据
        Message message = new SimpleMessage("xxxyyyzzz");
        LogEvent event = Log4jLogEvent.newBuilder()
                .setMessage(message)
                .build();

        // 执行测试
        Log4j2SecretCheckFilter.Result filterResult = filter.filter(event);

        // 验证结果
        assertEquals(Log4j2SecretCheckFilter.Result.NEUTRAL, filterResult);
    }
} 