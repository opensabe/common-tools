package io.github.opensabe.spring.boot.starter.rocketmq.test.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class MessageClassPojo {
    private final String text;
    private final Integer number;
    private final Long timestamp;
    private final Double cost;

    public MessageClassPojo() {
        this.text = "";
        this.number = 0;
        this.timestamp = 0L;
        this.cost = 0.0;
    }
}
