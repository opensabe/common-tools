package io.github.opensabe.spring.cloud.parent.gateway.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@NoArgsConstructor
@ConfigurationProperties("spring.cloud.gateway.batch-load-balance")
public class GatewayBatchLoadBalanceProperties {

    private Set<String> pathPatterns;
}
