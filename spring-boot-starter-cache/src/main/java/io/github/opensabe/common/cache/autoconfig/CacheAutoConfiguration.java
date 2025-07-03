package io.github.opensabe.common.cache.autoconfig;

import io.github.opensabe.common.cache.config.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * @author heng.ma
 */
@AutoConfiguration
@Import({
        ExpireCachingConfigurer.class,
        CacheAopConfiguration.class,
        CacheManagerConfiguration.class,
        RedisConfiguration.class,
        CaffeineConfiguration.class
})
@EnableConfigurationProperties(CachesProperties.class)
public class CacheAutoConfiguration {
    
}
