package io.github.opensabe.common.alive.client.message;

import io.github.opensabe.common.alive.client.message.enumeration.DevType;
import lombok.Data;

@Data
public class RegDev extends MqMessage{
    private Integer requestId;
    private Integer productCode;
    private String deviceId;
    private DevType devType;
    private String token;
}
