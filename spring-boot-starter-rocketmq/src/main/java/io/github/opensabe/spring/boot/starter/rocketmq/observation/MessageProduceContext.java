package io.github.opensabe.spring.boot.starter.rocketmq.observation;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageProduceContext extends Observation.Context {
    private final String topic;
    private long msgLength;
    private String sendResult = "";
    private Throwable throwable;

    public MessageProduceContext(String topic) {
        this.topic = topic == null ? "" : topic;
    }
}
