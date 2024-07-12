package io.github.opensabe.spring.boot.starter.rocketmq.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Category({"RocketMQ"})
@Label("Message Consume")
@StackTrace(false)
public class MessageConsume extends Event {
    @Getter
    private final String originTraceId;
    @Getter
    private final String traceId;
    @Getter
    private final String spanId;
    @Getter
    private final String topic;

    @Setter
    private Boolean successful;

    @Setter
    private Throwable throwable;


    public MessageConsume(String originTraceId, String traceId, String spanId, String topic) {
        this.originTraceId = originTraceId;
        this.traceId = traceId;
        this.spanId = spanId;
        this.topic = topic;
    }
}
