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
package io.github.opensabe.common.secret;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import io.github.opensabe.common.utils.SpringUtil;

@DisplayName("Log4j2密钥检查过滤器测试")
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
    @DisplayName("测试过滤器 - 包含敏感内容时应拒绝")
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
    @DisplayName("测试过滤器 - 非敏感内容时应通过")
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
    @DisplayName("测试过滤器 - 空消息时应通过")
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
    @DisplayName("测试过滤器 - 特殊消息时应通过")
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