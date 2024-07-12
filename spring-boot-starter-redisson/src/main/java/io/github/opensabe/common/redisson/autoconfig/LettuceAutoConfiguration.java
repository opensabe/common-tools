package io.github.opensabe.common.redisson.autoconfig;

import io.github.opensabe.common.redisson.config.LettuceConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Lettuce
 */
@AutoConfiguration
@Import({LettuceConfiguration.class})
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class LettuceAutoConfiguration {
}
