package io.github.opensabe.common.cache.aop;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;

public class CacheEvictAdvisor extends CacheAdvisor<CacheEvict> {

    public CacheEvictAdvisor(StringRedisTemplate template) {
        super(template);
    }

    @Override
    public String getCacheKey() {
        return getCacheOpt().key();
    }

    @Override
    public String getCacheName() {
        String[] value = getCacheOpt().value();
        if (value.length == 0) {
            value = getCacheOpt().cacheNames();
        }
        return value[0];
    }

    @Override
    public void exec(String cacheName, String key, long ttl) {
//        template.opsForHash().delete(cacheName, key);
    }
}
