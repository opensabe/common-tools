package io.github.opensabe.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

/**
 * 从 long 类型的时间戳反序列化为 LocalDateTime
 * long 类型只到毫秒时间
 */
@Log4j2
public class LongToLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    public static LongToLocalDateTimeDeserializer INSTANCE = new LongToLocalDateTimeDeserializer();

    private final ZoneId zoneId = ZoneId.systemDefault();

    private final List<DateTimeFormatter> formatters = new ArrayList<>(5);

    private final LocalDateTimeDeserializer delegate;

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
                }catch (Throwable e) {
                    return parse(string0.trim(), 0, null);
                }
            }
        };
    }


    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(p.getLongValue()), zoneId);
        } catch (Throwable e) {
            log.warn("LongToLocalDateTimeDeserializer-deserialize: Failed to deserialize LocalDateTime from long value: {}, {}", p, e);
            return delegate.deserialize(p, ctxt);
        }
    }

    private LocalDateTime parse (String str, int index, DateTimeParseException e) {
        if (index >= formatters.size()) {
            throw e;
        }
        try {
            return LocalDateTime.parse(str, formatters.get(index));
        }catch (DateTimeParseException e1) {
            return parse(str, ++index, e1);
        }
    }
}
