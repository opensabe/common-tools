package io.github.opensabe.common.redisson.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "spring.redis.redisson.aop")
public class RedissonAopConfiguration {
    private int order = Ordered.LOWEST_PRECEDENCE;
}
