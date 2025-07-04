package io.github.opensabe.common.cache.autoconfig;

import io.github.opensabe.common.cache.config.RedisConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author heng.ma
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@Import(RedisConfiguration.class)
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisCacheManagerAutoConfiguration {
}
