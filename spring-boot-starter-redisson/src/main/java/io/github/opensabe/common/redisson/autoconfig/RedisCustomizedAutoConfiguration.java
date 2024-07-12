package io.github.opensabe.common.redisson.autoconfig;

import io.github.opensabe.common.redisson.config.MultiRedisProperties;
import io.github.opensabe.common.redisson.config.RedisUtilConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisCustomizedConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({RedisCustomizedConfiguration.class, RedisUtilConfiguration.class})
@AutoConfigureBefore(RedisAutoConfiguration.class)
@EnableConfigurationProperties(MultiRedisProperties.class)
public class RedisCustomizedAutoConfiguration {
}
