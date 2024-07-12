package io.github.opensabe.common.alive.client.message;

import io.github.opensabe.common.alive.client.message.enumeration.PushType;
import io.github.opensabe.common.alive.client.message.enumeration.SubType;
import lombok.Data;

@Data
public class Subscribe extends MqMessage{
    private Integer requestId;

    private String topic;

    private SubType subType;

    private PushType pushType;

    private String accountId;

}
