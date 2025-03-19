package io.github.opensabe.common.alive.client.message;

import io.github.opensabe.common.alive.client.message.enumeration.DevType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RegDev extends MqMessage{
    private Integer requestId;
    private Integer productCode;
    private String deviceId;
    private DevType devType;
    private String token;
}
