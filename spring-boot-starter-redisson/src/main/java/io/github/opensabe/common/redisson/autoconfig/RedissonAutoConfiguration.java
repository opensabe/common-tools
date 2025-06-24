package io.github.opensabe.common.redisson.autoconfig;

import io.github.opensabe.common.redisson.config.RedissonAnnotationConfiguration;
import io.github.opensabe.common.redisson.config.RedissonAopOrderProperties;
import io.github.opensabe.common.redisson.config.RedissonClientBeanPostProcessor;
import io.github.opensabe.common.redisson.config.RedissonScheduleProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * Redisson 自动配置
 */
@EnableConfigurationProperties({RedissonScheduleProperties.class, RedissonAopOrderProperties.class})
@AutoConfiguration
@Import({RedissonClientBeanPostProcessor.class, RedissonAnnotationConfiguration.class})
public class RedissonAutoConfiguration {
}
