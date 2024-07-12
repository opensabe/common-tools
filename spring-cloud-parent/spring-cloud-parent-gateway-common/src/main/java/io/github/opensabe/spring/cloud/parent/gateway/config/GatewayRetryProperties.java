package io.github.opensabe.spring.cloud.parent.gateway.config;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@NoArgsConstructor
@ConfigurationProperties("spring.cloud.gateway.retry")
public class GatewayRetryProperties {
    /**
     * 可以重试的路径，支持 ant 匹配
     */
    private Set<String> retryablePathPatterns;
}
