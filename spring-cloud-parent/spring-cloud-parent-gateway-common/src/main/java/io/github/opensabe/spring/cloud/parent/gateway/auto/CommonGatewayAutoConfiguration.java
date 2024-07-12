package io.github.opensabe.spring.cloud.parent.gateway.auto;

import io.github.opensabe.spring.cloud.parent.gateway.config.CommonGatewayConfiguration;
import io.github.opensabe.spring.cloud.parent.gateway.config.DefaultFilterConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CommonGatewayConfiguration.class, DefaultFilterConfiguration.class})
public class CommonGatewayAutoConfiguration {
}
