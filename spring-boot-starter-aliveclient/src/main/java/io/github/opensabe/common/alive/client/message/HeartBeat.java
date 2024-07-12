package io.github.opensabe.common.alive.client.message;

import lombok.Data;

@Data
public class HeartBeat extends MqMessage{
    private Integer requestId;

    public HeartBeat(Integer requestId) {
        this.requestId = requestId;
    }

    public HeartBeat() {
    }
}
