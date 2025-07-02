package io.github.opensabe.common.cache.config;

import io.github.opensabe.common.cache.api.ExpireCacheResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.jcache.config.JCacheConfigurer;

/**
 * @author heng.ma
 */
public class ExpireCachingConfigurer implements CachingConfigurer, JCacheConfigurer, BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public ExpireCacheResolver cacheResolver() {
        return new ExpireCacheResolver(beanFactory);
    }
}
