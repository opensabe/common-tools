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
package io.github.opensabe.common.cache.compat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Spring Cache Redis {@link GenericJackson2JsonRedisSerializer} 写读契约：
 * 裸构造与带 {@link JavaTimeModule} 的行为差异及 Date round-trip。
 */
@DisplayName("Spring Cache Redis GenericJackson2 写读契约")
class GenericJackson2CacheWireContractTest {

    /**
     * Starter {@code RedisConfiguration} uses bare {@code new GenericJackson2JsonRedisSerializer()}.
     * LocalDateTime requires an ObjectMapper with {@link JavaTimeModule} (document / gate that gap).
     */
    private static RedisSerializer<Object> serializerWithJavaTime() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                ObjectMapper.DefaultTyping.EVERYTHING
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    /**
     * 与 starter 裸构造一致：LocalDateTime 序列化应失败。
     */
    @Test
    @DisplayName("带 JavaTimeModule 的 GenericJackson2：serialize → byte[] → deserialize")
    void bareSerializerRejectsLocalDateTime() {
        RedisSerializer<Object> bare = new GenericJackson2JsonRedisSerializer();
        CacheDtoWithLdt dto = new CacheDtoWithLdt("c1", LocalDateTime.of(2024, 6, 15, 10, 30), List.of("a"));
        assertThrows(Exception.class, () -> bare.serialize(dto));
    }

    /**
     * 注册 JavaTimeModule 后 LocalDateTime 可 serialize → deserialize round-trip。
     */
    @Test
    void roundTripWithJavaTimeModule() {
        RedisSerializer<Object> serializer = serializerWithJavaTime();
        CacheDtoWithLdt original = new CacheDtoWithLdt(
                "code-1",
                LocalDateTime.of(2024, 6, 15, 10, 30),
                List.of("id-1", "id-2")
        );

        byte[] stored = serializer.serialize(original);
        Object loaded = serializer.deserialize(stored);

        assertInstanceOf(CacheDtoWithLdt.class, loaded);
        CacheDtoWithLdt dto = (CacheDtoWithLdt) loaded;
        assertEquals(original.getCode(), dto.getCode());
        assertEquals(original.getCreatedAt(), dto.getCreatedAt());
        assertEquals(original.getChildren(), dto.getChildren());
        assertTrue(stored.length > 0);
    }

    /**
     * 裸构造 serializer 对 {@link Date} 字段可正常 round-trip。
     */
    @Test
    void bareSerializerRoundTripWithDate() {
        RedisSerializer<Object> bare = new GenericJackson2JsonRedisSerializer();
        CacheDtoWithDate original = new CacheDtoWithDate("code-2", new Date(1_700_000_000_000L), List.of("x"));
        byte[] stored = bare.serialize(original);
        Object loaded = bare.deserialize(stored);
        assertInstanceOf(CacheDtoWithDate.class, loaded);
        assertEquals(original.getCode(), ((CacheDtoWithDate) loaded).getCode());
        assertEquals(original.getCreatedAt().getTime(), ((CacheDtoWithDate) loaded).getCreatedAt().getTime());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheDtoWithLdt {
/** code。 */
        private String code;
/** createdAt。 */
        private LocalDateTime createdAt;
/** children。 */
        private List<String> children;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheDtoWithDate {
/** code。 */
        private String code;
/** createdAt。 */
        private Date createdAt;
/** children。 */
        private List<String> children;
    }
}
