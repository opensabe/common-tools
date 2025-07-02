package io.github.opensabe.common.cache.autoconfig;

import io.github.opensabe.common.cache.config.SpringCacheConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cache.annotation.ProxyCachingConfiguration;
import org.springframework.cache.jcache.config.ProxyJCacheConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author heng.ma
 */
@AutoConfiguration(after = {ProxyCachingConfiguration.class, ProxyJCacheConfiguration.class})
@Import(SpringCacheConfiguration.class)
public class CacheAutoConfiguration {
}
