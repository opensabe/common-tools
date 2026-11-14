package io.github.opensabe.spring.boot.starter.rocketmq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MQSendConfig {

    /**
     * 发送异常后持久化
     */
    @Builder.Default
    private Boolean persistence = true;

    /**
     * indicate whether the compression mechanism should be enabled, default false
     */
    @Builder.Default
    private Boolean isCompressEnabled = false;

    /**
     * RocketMQ delay level. null/0 = no delay; 1..18 = classic delay levels.
     * Actual delay duration is defined by Broker messageDelayLevel.
     */
    private Integer delayLevel;
}
