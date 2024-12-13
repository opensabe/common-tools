package io.github.opensabe.spring.boot.starter.rocketmq.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Category({"RocketMQ"})
@Label("Message Consume")
@StackTrace(false)
public class MessageConsume extends Event {
    private final String originTraceId;
    private final String topic;
    private String traceId;
    private String spanId;
    private Boolean successful;
    private Throwable throwable;


    public MessageConsume(String originTraceId, String topic) {
        this.originTraceId = originTraceId;
        this.topic = topic;
    }
}
