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
@Label("Message Producer")
@StackTrace(value = false)
public class MessageProduce extends Event {
    private final String topic;
    private long msgLength;
    private String traceId;
    private String spanId;
    private String sendResult;
    private Throwable throwable;


    public MessageProduce(String topic, long msgLength) {
        this.topic = topic;
        this.msgLength = msgLength;
    }

    public MessageProduce(String traceId, String spanId, String topic) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.topic = topic;
    }
}
