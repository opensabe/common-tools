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
package io.github.opensabe.common.jackson;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("时间戳模块测试")
class TimestampModuleTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new TimestampModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("测试LocalDateTime序列化 - 验证时间戳格式")
    void testSerializeLocalDateTime() throws Exception {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2024, 3, 15, 17, 30, 45, 123_000_000, ZoneId.of("UTC"));
        LocalDateTime dateTime = zonedDateTime.toLocalDateTime();
        String json = objectMapper.writeValueAsString(dateTime);
        assertTrue(StringUtils.isNumeric(json));
    }

    @Test
    @DisplayName("测试从长整型反序列化 - 验证毫秒时间戳转换")
    void testDeserializeFromLong() throws Exception {
        System.setProperty("user.timezone", "UTC");
        // 测试从毫秒时间戳反序列化
        String json = "1710495045123";
        LocalDateTime dateTime = objectMapper.readValue(json, LocalDateTime.class);
        assertNotNull(dateTime);
    }

    @Test
    @DisplayName("测试多种字符串格式反序列化 - 验证各种日期时间格式")
    void testDeserializeFromStringFormats() throws Exception {
        // 测试各种字符串格式的反序列化
        String[] formats = {
                "\"2024-03-15 14:30:45\"",
                "\"2024-03-15 14:30:45.123\"",
                "\"20240315143045\"",
                "\"20240315143045123\"",
                "\"20240315T14\"",
                "\"20240315T1430\"",
                "\"20240315T143045\"",
                "\"2024-03-15T14\"",
                "\"2024-03-15T14:30\"",
                "\"2024-03-15T14:30:45\""
        };

        for (String format : formats) {
            LocalDateTime dateTime = objectMapper.readValue(format, LocalDateTime.class);
            System.out.println("Deserialized LocalDateTime: " + dateTime);
            assertNotNull(dateTime);
        }
    }

    @Test
    @DisplayName("测试空值处理 - 验证null值处理")
    void testNullHandling() throws Exception {
        // 测试空值处理
        String json = "null";
        LocalDateTime dateTime = objectMapper.readValue(json, LocalDateTime.class);
        assertNull(dateTime);
    }

    @Test
    @DisplayName("测试往返序列化 - 验证序列化后反序列化的准确性")
    void testRoundTrip() throws Exception {
        // 测试序列化后反序列化的往返测试
        LocalDateTime original = LocalDateTime.now().withNano(0);
        String json = objectMapper.writeValueAsString(original);
        LocalDateTime deserialized = objectMapper.readValue(json, LocalDateTime.class);
        assertEquals(original, deserialized);
    }

    @Test
    @DisplayName("测试精度损失 - 验证纳秒到毫秒的截断")
    void testPrecisionLoss() throws Exception {
        // 测试精度损失（纳秒被截断到毫秒）
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_456_789);
        String json = objectMapper.writeValueAsString(dateTime);
        LocalDateTime deserialized = objectMapper.readValue(json, LocalDateTime.class);

        // 验证纳秒部分被截断到毫秒
        assertEquals(123_000_000, deserialized.getNano());
    }
} 