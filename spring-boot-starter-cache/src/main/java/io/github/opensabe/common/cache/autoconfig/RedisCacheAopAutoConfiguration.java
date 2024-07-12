package io.github.opensabe.common.cache.autoconfig;

import io.github.opensabe.common.cache.config.RedisCacheAopConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({RedisCacheAopConfiguration.class})
@ConditionalOnProperty(prefix = "caches", name = "enabled")
public class RedisCacheAopAutoConfiguration {
}
