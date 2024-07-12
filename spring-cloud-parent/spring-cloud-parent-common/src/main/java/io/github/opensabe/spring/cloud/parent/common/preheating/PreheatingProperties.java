package io.github.opensabe.spring.cloud.parent.common.preheating;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "preheating")
public class PreheatingProperties {
    /**
     * 预热是否启动
     */
    private boolean enabled;
    private Duration delayReadyTime;
}
