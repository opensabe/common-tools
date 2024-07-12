package io.github.opensabe.common.redisson.autoconfig;

import io.github.opensabe.common.redisson.config.RedissonAnnotationConfiguration;
import io.github.opensabe.common.redisson.config.RedissonConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Redisson 自动配置
 */
@AutoConfiguration
@Import({RedissonConfiguration.class, RedissonAnnotationConfiguration.class})
public class RedissonAutoConfiguration {
}
