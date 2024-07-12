package io.github.opensabe.scheduler.conf;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "scheduler.job")
public class SchedulerProperties {
    /**
     * 是否启用 scheduler，默认启用，可以通过配置关闭
     */
    private boolean enable = true;
    private Long expiredTime;
}
