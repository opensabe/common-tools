package io.github.opensabe.common.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * LocalDateTime 序列化为毫秒时间戳
 * 注意，这里的时间戳是毫秒级别的，LocalDateTime 是纳秒级别的，所以会丢失精度
 */
public class TimestampLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    public static final TimestampLocalDateTimeSerializer INSTANCE = new TimestampLocalDateTimeSerializer();

    private final ZoneId zoneId;

    public TimestampLocalDateTimeSerializer() {
        this.zoneId = ZoneId.systemDefault();
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (Objects.nonNull(value)) {
            gen.writeNumber(value.atZone(zoneId).toInstant().toEpochMilli());
        }
    }
}
