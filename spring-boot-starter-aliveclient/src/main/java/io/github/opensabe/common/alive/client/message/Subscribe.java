package io.github.opensabe.common.alive.client.message;

import io.github.opensabe.common.alive.client.message.enumeration.PushType;
import io.github.opensabe.common.alive.client.message.enumeration.SubType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Subscribe extends MqMessage{
    private Integer requestId;

    private String topic;

    private SubType subType;

    private PushType pushType;

    private String accountId;

}
