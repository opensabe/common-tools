package io.github.opensabe.common.alive.client.message;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Closed extends MqMessage{
    private String deviceId;

}
