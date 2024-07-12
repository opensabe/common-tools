package io.github.opensabe.common.cache.autoconfig;

import io.github.opensabe.common.cache.config.CustomCacheConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnProperty(prefix = "caches", name = "enabled")
@Import({CacheAutoConfiguration.class, CustomCacheConfiguration.class})
@EnableCaching
public class CustomCacheAutoConfiguration {
}
