package io.github.opensabe.common.cache.autoconfig;

import io.github.opensabe.common.cache.config.CachesProperties;
import io.github.opensabe.common.cache.config.ExpireCachingConfigurer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.ProxyCachingConfiguration;
import org.springframework.cache.jcache.config.ProxyJCacheConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author heng.ma
 */
@AutoConfiguration(before = {ProxyCachingConfiguration.class, ProxyJCacheConfiguration.class})
@Import(ExpireCachingConfigurer.class)
@EnableConfigurationProperties(CachesProperties.class)
public class CacheConfigurerConfiguration {
}
