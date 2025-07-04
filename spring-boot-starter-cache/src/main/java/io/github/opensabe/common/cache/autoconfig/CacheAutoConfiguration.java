package io.github.opensabe.common.cache.autoconfig;

import io.github.opensabe.common.cache.config.CacheAopConfiguration;
import io.github.opensabe.common.cache.config.CacheManagerConfiguration;
import io.github.opensabe.common.cache.config.CachesProperties;
import io.github.opensabe.common.cache.config.CaffeineConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;


/**
 * @author heng.ma
 */
@AutoConfiguration
@Import({
        CacheAopConfiguration.class,
        CacheManagerConfiguration.class,
        CaffeineConfiguration.class
})
@EnableConfigurationProperties(CachesProperties.class)
public class CacheAutoConfiguration {

}
