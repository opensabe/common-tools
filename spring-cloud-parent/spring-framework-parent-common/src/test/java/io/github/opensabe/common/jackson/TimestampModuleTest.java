package io.github.opensabe.common.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimestampModuleTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new TimestampModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void testSerializeLocalDateTime() throws Exception {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2024, 3, 15, 17, 30, 45, 123_000_000, ZoneId.of("UTC"));
        LocalDateTime dateTime = zonedDateTime.toLocalDateTime();
        String json = objectMapper.writeValueAsString(dateTime);
        assertTrue(StringUtils.isNumeric(json));
    }

    @Test
    void testDeserializeFromLong() throws Exception {
        System.setProperty("user.timezone", "UTC");
        // 测试从毫秒时间戳反序列化
        String json = "1710495045123";
        LocalDateTime dateTime = objectMapper.readValue(json, LocalDateTime.class);
        assertNotNull(dateTime);
    }

    @Test
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
    void testNullHandling() throws Exception {
        // 测试空值处理
        String json = "null";
        LocalDateTime dateTime = objectMapper.readValue(json, LocalDateTime.class);
        assertNull(dateTime);
    }

    @Test
    void testRoundTrip() throws Exception {
        // 测试序列化后反序列化的往返测试
        LocalDateTime original = LocalDateTime.now().withNano(0);
        String json = objectMapper.writeValueAsString(original);
        LocalDateTime deserialized = objectMapper.readValue(json, LocalDateTime.class);
        assertEquals(original, deserialized);
    }

    @Test
    void testPrecisionLoss() throws Exception {
        // 测试精度损失（纳秒被截断到毫秒）
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_456_789);
        String json = objectMapper.writeValueAsString(dateTime);
        LocalDateTime deserialized = objectMapper.readValue(json, LocalDateTime.class);
        
        // 验证纳秒部分被截断到毫秒
        assertEquals(123_000_000, deserialized.getNano());
    }
} 