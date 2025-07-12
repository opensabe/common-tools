package io.github.opensabe.common.secret;

import io.github.opensabe.common.utils.AlarmUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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