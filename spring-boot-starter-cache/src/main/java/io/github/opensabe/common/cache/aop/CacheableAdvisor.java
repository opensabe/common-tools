package io.github.opensabe.common.cache.aop;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;

public class CacheableAdvisor extends CacheAdvisor<Cacheable> {

    public CacheableAdvisor(StringRedisTemplate template) {
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
//        template.opsForHash().putIfAbsent(cacheName, key, Timestamp.valueOf(LocalDateTime.now().plusSeconds(ttl)).toString());
    }
}
