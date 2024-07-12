package io.github.opensabe.alive.client.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MQTopic {

    BROAD_CAST("opensabe_common_alive_broadcast"),

    SIMPLE("opensabe_common_alive_simple"),

    ;

    private String topic;
}
