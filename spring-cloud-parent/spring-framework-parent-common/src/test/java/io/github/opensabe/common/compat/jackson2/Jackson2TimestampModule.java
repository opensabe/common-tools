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
package io.github.opensabe.common.compat.jackson2;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * 测试专用 Jackson 2 版 {@link io.github.opensabe.common.jackson.TimestampModule} 镜像（LocalDateTime ↔ epoch ms）。
 */
public final class Jackson2TimestampModule extends SimpleModule {

    public Jackson2TimestampModule() {
        addSerializer(LocalDateTime.class, new JsonSerializer<>() {
            private final ZoneId zoneId = ZoneId.systemDefault();

            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (Objects.nonNull(value)) {
                    gen.writeNumber(value.atZone(zoneId).toInstant().toEpochMilli());
                }
            }
        });
        addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
            private final ZoneId zoneId = ZoneId.systemDefault();

            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                if (p.currentToken().isNumeric()) {
                    return LocalDateTime.ofInstant(Instant.ofEpochMilli(p.getLongValue()), zoneId);
                }
                String text = p.getValueAsString();
                if (text == null || text.isBlank()) {
                    return null;
                }
                try {
                    return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(text.trim())), zoneId);
                } catch (NumberFormatException ignored) {
                    return LocalDateTime.parse(text.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
            }
        });
    }
}
