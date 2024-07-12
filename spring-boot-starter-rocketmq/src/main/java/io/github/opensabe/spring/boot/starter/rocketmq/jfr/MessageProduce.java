package io.github.opensabe.spring.boot.starter.rocketmq.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Category({"RocketMQ"})
@Label("Message Producer")
@StackTrace(value = true)
public class MessageProduce extends Event {
    @Getter
    private final String traceId;
    @Getter
    private final String spanId;
    @Getter
    private final String topic;
    @Setter
    private String sendResult;
    @Setter
    private Throwable throwable;


    public MessageProduce(String traceId, String spanId, String topic) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.topic = topic;
    }
}
