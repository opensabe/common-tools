package io.github.opensabe.common.redisson.config;

import io.github.opensabe.common.redisson.util.LuaLimitCache;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
public class RedisUtilConfiguration {
    @Bean
    public LuaLimitCache luaLimitCache(StringRedisTemplate redisTemplate, RedissonClient redissonClient) {
        return new LuaLimitCache(redisTemplate, redissonClient);
    }
}
