package io.github.opensabe.common.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;

/**
 * 时间戳序列化模块
 */
public class TimestampModule extends SimpleModule {

    public TimestampModule() {
        //LocalDateTime 与 Long 序列化，反序列化，会转换为毫秒时间戳，毫秒以下的时间戳会被忽略
        //所以，LocalDateTime 最好在生成的时候 truncate 到毫秒，比如：
        //LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        addSerializer(LocalDateTime.class, TimestampLocalDateTimeSerializer.INSTANCE);
        addDeserializer(LocalDateTime.class, LongToLocalDateTimeDeserializer.INSTANCE);
    }
}
