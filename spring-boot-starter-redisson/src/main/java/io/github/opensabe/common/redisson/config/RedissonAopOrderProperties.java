package io.github.opensabe.common.redisson.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "spring.redis.redisson.aop")
public class RedissonAopOrderProperties {
    private int order = Ordered.LOWEST_PRECEDENCE;
}
