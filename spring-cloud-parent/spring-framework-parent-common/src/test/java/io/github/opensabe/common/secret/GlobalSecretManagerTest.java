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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

import io.github.opensabe.common.utils.AlarmUtil;

@DisplayName("全局密钥管理器测试")
class GlobalSecretManagerTest {

    private GlobalSecretManager secretManager;

    @BeforeEach
    void setUp() {
        secretManager = new GlobalSecretManager();
    }

    @Test
    @DisplayName("测试添加密钥 - 验证敏感信息过滤")
    void testPutSecret() {
        // 准备测试数据
        Map<String, Set<String>> secret = new HashMap<>();
        Set<String> values = new HashSet<>();
        values.add("password123");
        values.add("secret456");
        secret.put("credentials", values);

        // 执行测试
        secretManager.putSecret("testProvider", secret);

        // 验证结果
        FilterSecretStringResult result = secretManager.filterSecretStringAndAlarm("This is a test with password123");
        assertTrue(result.isFoundSensitiveString());
        assertEquals("This is a test with ******", result.getFilteredContent());
    }

    @Test
    @DisplayName("测试敏感信息过滤 - 无敏感内容")
    void testFilterSecretStringAndAlarm_NoSensitiveContent() {
        // 准备测试数据
        Map<String, Set<String>> secret = new HashMap<>();
        Set<String> values = new HashSet<>();
        values.add("password123");
        secret.put("credentials", values);
        secretManager.putSecret("testProvider", secret);

        // 执行测试
        FilterSecretStringResult result = secretManager.filterSecretStringAndAlarm("This is a normal message");

        // 验证结果
        assertFalse(result.isFoundSensitiveString());
        assertEquals("This is a normal message", result.getFilteredContent());
    }

    @Test
    @DisplayName("测试敏感信息过滤 - 多个敏感内容")
    void testFilterSecretStringAndAlarm_MultipleSensitiveContent() {
        // 准备测试数据
        Map<String, Set<String>> secret = new HashMap<>();
        Set<String> values = new HashSet<>();
        values.add("password123");
        values.add("secret456");
        secret.put("credentials", values);
        secretManager.putSecret("testProvider", secret);

        // 执行测试
        FilterSecretStringResult result = secretManager.filterSecretStringAndAlarm("password123 and secret456 found");

        // 验证结果
        assertTrue(result.isFoundSensitiveString());
        assertEquals("****** and ****** found", result.getFilteredContent());
    }

    @Test
    @DisplayName("测试敏感信息过滤和告警 - 验证告警触发")
    void testFilterSecretStringAndAlarm_WithAlarm() {
        try (MockedStatic<AlarmUtil> alarmUtilMockedStatic = Mockito.mockStatic(AlarmUtil.class)) {
            // 准备测试数据
            Map<String, Set<String>> secret = new HashMap<>();
            Set<String> values = new HashSet<>();
            values.add("password123");
            secret.put("credentials", values);
            secretManager.putSecret("testProvider", secret);

            // 执行测试
            secretManager.filterSecretStringAndAlarm("Found password123");

            // 验证告警是否被调用
            alarmUtilMockedStatic.verify(() ->
                    AlarmUtil.fatal(anyString(), anyString(), anyString())
            );
        }
    }
} 