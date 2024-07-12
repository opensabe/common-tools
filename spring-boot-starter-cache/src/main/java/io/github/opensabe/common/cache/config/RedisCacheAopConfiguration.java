package io.github.opensabe.common.cache.config;

import io.github.opensabe.common.cache.aop.CacheEvictAdvisor;
import io.github.opensabe.common.cache.aop.CachePutAdvisor;
import io.github.opensabe.common.cache.aop.CacheableAdvisor;
import io.github.opensabe.common.cache.aop.CustomCachePointcut;
import io.github.opensabe.common.cache.cache.RedisCustomCache;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
//有这个类，一定引入了 Redis + Redisson 相关依赖使用了 Redis，所以就初始化 Redis 缓存
@ConditionalOnClass(name = "io.github.opensabe.common.redisson.autoconfig.RedissonAutoConfiguration")
public class RedisCacheAopConfiguration {

    @Bean("redis")
    public RedisCustomCache redisCustomCache(CacheManagerCustomizers cacheManagerCustomizers, ObjectProvider<RedisCacheConfiguration> redisCacheConfiguration, ObjectProvider<RedisCacheManagerBuilderCustomizer> redisCacheManagerBuilderCustomizers, RedisConnectionFactory redisConnectionFactory){
        return new RedisCustomCache(cacheManagerCustomizers, redisCacheConfiguration, redisCacheManagerBuilderCustomizers, redisConnectionFactory);
    }

    @Bean
    public CustomCachePointcut customCachePointcut(CompositeCacheManager cacheManager, ApplicationContext context) {
        return new CustomCachePointcut(cacheManager, context);
    }

    @Bean("cacheable")
    @Scope("prototype")
    public CacheableAdvisor cacheableAdvisor(StringRedisTemplate template) {
        return new CacheableAdvisor(template);
    }

    @Bean("cacheput")
    @Scope("prototype")
    public CachePutAdvisor cachePutAdvisor(StringRedisTemplate template) {
        return new CachePutAdvisor(template);
    }

    @Bean("cacheevict")
    @Scope("prototype")
    public CacheEvictAdvisor cacheEvictAdvisor(StringRedisTemplate template) {
        return new CacheEvictAdvisor(template);
    }

//    @Bean
//    public ExpireScheduler expireScheduler(StringRedisTemplate template, CompositeCacheManager cacheManager) {
//        return new ExpireScheduler(template, cacheManager);
//    }
}
