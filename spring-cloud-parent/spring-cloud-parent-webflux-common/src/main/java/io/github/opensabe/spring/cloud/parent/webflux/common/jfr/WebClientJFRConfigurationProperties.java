package io.github.opensabe.spring.cloud.parent.webflux.common.jfr;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "webclient.jfr")
public class WebClientJFRConfigurationProperties {
    /**
     * 是否开启 JFR 事件生成
     */
    private boolean enabled = true;
}
