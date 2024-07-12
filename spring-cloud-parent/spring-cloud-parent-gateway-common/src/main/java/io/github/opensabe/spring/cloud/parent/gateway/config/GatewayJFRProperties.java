package io.github.opensabe.spring.cloud.parent.gateway.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties("spring.cloud.gateway.jfr")
public class GatewayJFRProperties {
    private boolean enabled = true;
}
