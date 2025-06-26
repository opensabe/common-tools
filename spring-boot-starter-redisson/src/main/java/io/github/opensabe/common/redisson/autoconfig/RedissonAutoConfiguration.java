package io.github.opensabe.common.redisson.autoconfig;

import io.github.opensabe.common.redisson.config.RedissonAnnotationConfiguration;
import io.github.opensabe.common.redisson.config.RedissonAopOrderProperties;
import io.github.opensabe.common.redisson.config.RedissonClientBeanPostProcessor;
import io.github.opensabe.common.redisson.config.RedissonScheduleProperties;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 这里必须手动调整顺序，RedisAutoConfiguration 必须在 RedissonAutoConfigurationV2 之前执行
 * 否则RedissonAutoConfigurationV2会覆盖RedisAutoConfiguration的配置，RedisConnectionFactory会被替换为RedissonConnectionFactory.
 * Redisson 自动配置
 */
@EnableConfigurationProperties({RedissonScheduleProperties.class, RedissonAopOrderProperties.class})
@AutoConfiguration(before = RedissonAutoConfigurationV2.class)
@Import({RedissonClientBeanPostProcessor.class, RedissonAnnotationConfiguration.class, RedisAutoConfiguration.class})
public class RedissonAutoConfiguration {
}
