package io.github.opensabe.spring.cloud.parent.gateway.config;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@NoArgsConstructor
@ConfigurationProperties("spring.cloud.gateway.uid-load-balance")
public class GatewayLoadBalanceByUidProperties {
    /**
     * 默认只有 GET 方法会使用 uid 负载均衡
     * 这里额外将一些路径使用 uid 负载均衡，支持 ant 匹配
     */
    private Set<String> pathPatterns;
}
