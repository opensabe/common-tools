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
import java.util.Objects;

import lombok.Getter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * 将 {@link LocalDateTime} 序列化为毫秒级 epoch 时间戳。
 * <p>
 * {@link LocalDateTime} 精度为纳秒，转换为毫秒时会截断亚毫秒部分；建议在写入前
 * {@code truncatedTo(ChronoUnit.MILLIS)}。
 */
public class TimestampLocalDateTimeSerializer extends ValueSerializer<LocalDateTime> {

    /** 单例实例，供 {@link TimestampModule} 注册使用。 */
    @Getter
    private static final TimestampLocalDateTimeSerializer INSTANCE = new TimestampLocalDateTimeSerializer();

    /** 本地日期时间转换为 epoch 毫秒时使用的系统默认时区。 */
    private final ZoneId zoneId;

    /**
     * 使用系统默认时区构造序列化器。
     */
    public TimestampLocalDateTimeSerializer() {
        this.zoneId = ZoneId.systemDefault();
    }

    /**
     * 将非 null 的 {@link LocalDateTime} 写为 JSON 数值（毫秒 epoch）。
     *
     * @param value       待序列化的本地日期时间
     * @param gen         JSON 生成器
     * @param serializers 序列化上下文
     */
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext serializers) {
        if (Objects.nonNull(value)) {
            gen.writeNumber(value.atZone(zoneId).toInstant().toEpochMilli());
        }
    }
}
