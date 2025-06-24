package io.github.opensabe.common.redisson.autoconfig;

import io.github.opensabe.common.redisson.config.MultiRedisProperties;
import io.github.opensabe.common.redisson.config.RedisConfiguration;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import io.github.opensabe.common.redisson.config.MultiRedisConnectionFactoryConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration(before = RedissonAutoConfiguration.class)
@Import({MultiRedisConnectionFactoryConfiguration.class, RedisConfiguration.class})
@EnableConfigurationProperties(MultiRedisProperties.class)
public class MultiRedisAutoConfiguration {
}
