package io.github.opensabe.common.cache.aop;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.annotation.Annotation;

public abstract class CacheAdvisor<T> {

    private T cacheOpt;
    protected StringRedisTemplate template;

    public CacheAdvisor(StringRedisTemplate template) {
        this.template = template;
    }

    public void setCacheOpt(Annotation cacheOpt) {
        this.cacheOpt = (T) cacheOpt;
    }

    public T getCacheOpt() {
        return cacheOpt;
    }

    public abstract String getCacheKey();

    public abstract String getCacheName();

    public abstract void exec(String cacheName, String key, long ttl);
}
