package io.github.opensabe.spring.boot.starter.rocketmq.observation;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageConsumeContext extends Observation.Context {
    private final String originTraceId;
    private final String topic;
    private Boolean successful = false;
    private Throwable throwable;

    public MessageConsumeContext(String originTraceId, String topic) {
        this.originTraceId = originTraceId;
        this.topic = topic;
    }
}
