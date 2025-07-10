package io.github.opensabe.common.cache.config;

import io.github.opensabe.common.cache.api.ExpireCacheResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.cache.jcache.config.JCacheConfigurer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator(){
            @Override
            public Object generate(Object target, Method method, Object... params) {
                if (params.length == 0) {
                    return method.getName();
                }else if (params.length == 1) {
                    Object param = params[0];
                    if (param != null && !param.getClass().isArray()) {
                        return param;
                    }
                }
                return Arrays.stream(params).map(Objects::toString).collect(Collectors.joining(":"));
            }
        };
    }
}
