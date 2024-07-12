package io.github.opensabe.common.redisson.config;

import io.github.opensabe.common.redisson.aop.RedissonLockAdvisor;
import io.github.opensabe.common.redisson.aop.RedissonLockCachedPointcut;
import io.github.opensabe.common.redisson.aop.RedissonLockInterceptor;
import io.github.opensabe.common.redisson.aop.RedissonRateLimiterAdvisor;
import io.github.opensabe.common.redisson.aop.RedissonRateLimiterCachedPointcut;
import io.github.opensabe.common.redisson.aop.RedissonRateLimiterInterceptor;
import io.github.opensabe.common.redisson.aop.RedissonScheduledBeanPostProcessor;
import io.github.opensabe.common.redisson.aop.RedissonScheduledListener;
import io.github.opensabe.common.redisson.aop.RedissonSemaphoreAdvisor;
import io.github.opensabe.common.redisson.aop.RedissonSemaphoreCachedPointcut;
import io.github.opensabe.common.redisson.aop.RedissonSemaphoreInterceptor;
import io.github.opensabe.common.redisson.jfr.RExpirableExpireObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RLockAcquiredObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RLockForceReleaseObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RLockReleasedObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RPermitSemaphoreAcquiredObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RPermitSemaphoreModifiedObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RPermitSemaphoreReleasedObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RRateLimiterAcquireObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RRateLimiterSetRateObservationToJFRGenerator;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RedissonAnnotationConfiguration {

    @Bean
    public RedissonLockCachedPointcut redissonLockCachedPointcut() {
        return new RedissonLockCachedPointcut();
    }

    @Bean
    public RedissonLockInterceptor redissonLockInterceptor(
            RedissonClient redissonClient,
            RedissonLockCachedPointcut redissonLockCachedPointcut,
            UnifiedObservationFactory unifiedObservationFactory
    ) {
        return new RedissonLockInterceptor(redissonClient, redissonLockCachedPointcut, unifiedObservationFactory);
    }

    @Bean
    public RedissonLockAdvisor redissonLockAdvisor(RedissonLockCachedPointcut redissonLockCachedPointcut, RedissonLockInterceptor redissonLockInterceptor, RedissonAopConfiguration redissonAopConfiguration) {
        var advisor = new RedissonLockAdvisor(redissonLockCachedPointcut);
        advisor.setAdvice(redissonLockInterceptor);
        advisor.setOrder(redissonAopConfiguration.getOrder());
        return advisor;
    }

    @Bean
    public RedissonRateLimiterCachedPointcut redissonRateLimiterCachedPointcut() {
        return new RedissonRateLimiterCachedPointcut();
    }

    @Bean
    public RedissonRateLimiterInterceptor redissonRateLimiterInterceptor(RedissonClient redissonClient, RedissonRateLimiterCachedPointcut redissonRateLimiterCachedPointcut, UnifiedObservationFactory unifiedObservationFactory) {
        return new RedissonRateLimiterInterceptor(redissonClient, redissonRateLimiterCachedPointcut, unifiedObservationFactory);
    }

    @Bean
    public RedissonRateLimiterAdvisor redissonRateLimiterAdvisor(RedissonRateLimiterCachedPointcut redissonRateLimiterCachedPointcut, RedissonRateLimiterInterceptor redissonRateLimiterInterceptor, RedissonAopConfiguration redissonAopConfiguration) {
        var advisor = new RedissonRateLimiterAdvisor(redissonRateLimiterCachedPointcut);
        advisor.setAdvice(redissonRateLimiterInterceptor);
        advisor.setOrder(redissonAopConfiguration.getOrder());
        return advisor;
    }

    @Bean
    public RedissonSemaphoreCachedPointcut redissonSemaphoreCachedPointcut() {
        return new RedissonSemaphoreCachedPointcut();
    }

    @Bean
    public RedissonSemaphoreInterceptor redissonSemaphoreInterceptor(RedissonClient redissonClient, RedissonSemaphoreCachedPointcut redissonSemaphoreCachedPointcut, UnifiedObservationFactory unifiedObservationFactory) {
        return new RedissonSemaphoreInterceptor(redissonClient, redissonSemaphoreCachedPointcut, unifiedObservationFactory);
    }

    @Bean
    public RedissonSemaphoreAdvisor redissonSemaphoreAdvisor(RedissonSemaphoreCachedPointcut redissonSemaphoreCachedPointcut, RedissonSemaphoreInterceptor redissonSemaphoreInterceptor, RedissonAopConfiguration redissonAopConfiguration) {
        var advisor = new RedissonSemaphoreAdvisor(redissonSemaphoreCachedPointcut);
        advisor.setAdvice(redissonSemaphoreInterceptor);
        advisor.setOrder(redissonAopConfiguration.getOrder());
        return advisor;
    }


    @Bean
    @ConditionalOnMissingBean
    public RedissonScheduledBeanPostProcessor redissonScheduledBeanPostProcessor(RedissonProperties redissonProperties) {
        return new RedissonScheduledBeanPostProcessor(redissonProperties);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public RedissonScheduledListener redissonScheduledListener(RedissonScheduledBeanPostProcessor redissonScheduledBeanPostProcessor, UnifiedObservationFactory unifiedObservationFactory, RedissonClient redissonClient, MeterRegistry meterRegistry) {
        return new RedissonScheduledListener(redissonScheduledBeanPostProcessor, unifiedObservationFactory, redissonClient, meterRegistry);
    }

    @Bean
    public RExpirableExpireObservationToJFRGenerator rExpirableExpireObservationToJFRGenerator() {
        return new RExpirableExpireObservationToJFRGenerator();
    }

    @Bean
    public RLockAcquiredObservationToJFRGenerator rLockAcquiredObservationToJFRGenerator() {
        return new RLockAcquiredObservationToJFRGenerator();
    }

    @Bean
    public RLockForceReleaseObservationToJFRGenerator rLockForceReleaseObservationToJFRGenerator() {
        return new RLockForceReleaseObservationToJFRGenerator();
    }

    @Bean
    public RLockReleasedObservationToJFRGenerator rLockReleasedObservationToJFRGenerator() {
        return new RLockReleasedObservationToJFRGenerator();
    }

    @Bean
    public RPermitSemaphoreAcquiredObservationToJFRGenerator rPermitSemaphoreAcquiredObservationToJFRGenerator() {
        return new RPermitSemaphoreAcquiredObservationToJFRGenerator();
    }

    @Bean
    public RPermitSemaphoreModifiedObservationToJFRGenerator rPermitSemaphoreModifiedObservationToJFRGenerator() {
        return new RPermitSemaphoreModifiedObservationToJFRGenerator();
    }

    @Bean
    public RPermitSemaphoreReleasedObservationToJFRGenerator rPermitSemaphoreReleasedObservationToJFRGenerator() {
        return new RPermitSemaphoreReleasedObservationToJFRGenerator();
    }

    @Bean
    public RRateLimiterAcquireObservationToJFRGenerator rRateLimiterAcquireObservationToJFRGenerator() {
        return new RRateLimiterAcquireObservationToJFRGenerator();
    }

    @Bean
    public RRateLimiterSetRateObservationToJFRGenerator rRateLimiterSetRateObservationToJFRGenerator() {
        return new RRateLimiterSetRateObservationToJFRGenerator();
    }
}
