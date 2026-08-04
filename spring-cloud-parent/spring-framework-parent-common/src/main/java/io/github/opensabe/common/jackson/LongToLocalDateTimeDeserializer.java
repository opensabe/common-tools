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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;

/**
 * 将 JSON 数值（毫秒 epoch）或多种字符串格式反序列化为 {@link LocalDateTime}。
 * <p>
 * 优先按 long 毫秒时间戳解析；失败时回退到 ISO 及常见业务日期字符串格式。
 */
@Log4j2
public class LongToLocalDateTimeDeserializer extends ValueDeserializer<LocalDateTime> {

    /** 单例实例，供 {@link TimestampModule} 注册使用。 */
    @Getter
    private static final LongToLocalDateTimeDeserializer INSTANCE = new LongToLocalDateTimeDeserializer();

    /** 毫秒时间戳转换为 {@link LocalDateTime} 时使用的系统默认时区。 */
    private final ZoneId zoneId = ZoneId.systemDefault();

    /** 字符串回退解析时按顺序尝试的日期时间格式器列表。 */
    private final List<DateTimeFormatter> formatters = new ArrayList<>(5);

    /** JSR-310 默认反序列化器，字符串回退的第一选择。 */
    private final LocalDateTimeDeserializer delegate;

    /**
     * 初始化格式器列表与委托反序列化器。
     */
    public LongToLocalDateTimeDeserializer() {
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        formatters.add(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        formatters.add(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        formatters.add(new DateTimeFormatterBuilder()
                .appendPattern("yyyyMMdd[['T'HH][mm][ss]]")
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                .toFormatter());
        formatters.add(new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd[['T'HH][:mm][:ss]]")
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                .toFormatter());

        delegate = new LocalDateTimeDeserializer() {
            @Override
            protected LocalDateTime _fromString(JsonParser p, DeserializationContext ctxt, String string0) {
                try {
                    return super._fromString(p, ctxt, string0);
                } catch (Throwable e) {
                    return parse(string0.trim(), 0, null);
                }
            }
        };
    }

    /**
     * 反序列化 JSON 值为 {@link LocalDateTime}。
     * <p>
     * 数值 token 按毫秒 epoch 解析；其他 token 委托 {@link #delegate} 并按 {@link #formatters} 逐级回退。
     *
     * @param p    JSON 解析器
     * @param ctxt 反序列化上下文
     * @return 解析得到的本地日期时间
     */
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) {
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(p.getLongValue()), zoneId);
        } catch (Throwable e) {
            log.warn("LongToLocalDateTimeDeserializer-deserialize: Failed to deserialize LocalDateTime from long value: {}, {}", p, e);
            return delegate.deserialize(p, ctxt);
        }
    }

    /**
     * 按 {@link #formatters} 顺序尝试解析日期字符串。
     *
     * @param str   待解析字符串
     * @param index 当前尝试的格式器下标
     * @param e     上一次解析失败异常，全部失败时重新抛出
     * @return 解析得到的本地日期时间
     * @throws DateTimeParseException 所有格式均无法解析时
     */
    private LocalDateTime parse(String str, int index, DateTimeParseException e) {
        if (index >= formatters.size()) {
            throw e;
        }
        try {
            return LocalDateTime.parse(str, formatters.get(index));
        } catch (DateTimeParseException e1) {
            return parse(str, ++index, e1);
        }
    }
}
