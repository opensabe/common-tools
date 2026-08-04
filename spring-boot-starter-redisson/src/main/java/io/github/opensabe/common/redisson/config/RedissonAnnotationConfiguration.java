/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.redisson.config;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.aop.lock.RedissonLockAdvisor;
import io.github.opensabe.common.redisson.aop.lock.RedissonLockCachedPointcut;
import io.github.opensabe.common.redisson.aop.lock.RedissonLockInterceptor;
import io.github.opensabe.common.redisson.aop.ratelimiter.RedissonRateLimiterAdvisor;
import io.github.opensabe.common.redisson.aop.ratelimiter.RedissonRateLimiterCachedPointcut;
import io.github.opensabe.common.redisson.aop.ratelimiter.RedissonRateLimiterInterceptor;
import io.github.opensabe.common.redisson.aop.scheduled.RedissonScheduledBeanPostProcessor;
import io.github.opensabe.common.redisson.aop.scheduled.RedissonScheduledListener;
import io.github.opensabe.common.redisson.aop.scheduled.RedissonScheduledService;
import io.github.opensabe.common.redisson.aop.semaphore.RedissonSemaphoreAdvisor;
import io.github.opensabe.common.redisson.aop.semaphore.RedissonSemaphoreCachedPointcut;
import io.github.opensabe.common.redisson.aop.semaphore.RedissonSemaphoreInterceptor;
import io.github.opensabe.common.redisson.aop.slock.SLockAdvisor;
import io.github.opensabe.common.redisson.aop.slock.SLockInterceptor;
import io.github.opensabe.common.redisson.aop.slock.SLockPointcut;
import io.github.opensabe.common.redisson.jfr.RExpirableExpireObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RLockAcquiredObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RLockForceReleaseObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RLockReleasedObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RPermitSemaphoreAcquiredObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RPermitSemaphoreModifiedObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RPermitSemaphoreReleasedObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RRateLimiterAcquireObservationToJFRGenerator;
import io.github.opensabe.common.redisson.jfr.RRateLimiterSetRateObservationToJFRGenerator;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Redisson 注解驱动 AOP 与 JFR 观测 Bean 注册中心。
 * <p>
 * 统一装配锁、限流、信号量、定时任务切点/拦截器/Advisor，以及 Observation → JFR 生成器。
 */
@Configuration(proxyBeanMethods = false)
public class RedissonAnnotationConfiguration {

    /** 方法参数 SpEL 求值器。 */
    @Bean
    public MethodArgumentsExpressEvaluator methodArgumentsExpressEvaluator(BeanFactory beanFactory) {
        return new MethodArgumentsExpressEvaluator(beanFactory);
    }

    /** 旧版分布式锁切点。 */
    @Bean
    public RedissonLockCachedPointcut redissonLockCachedPointcut(MethodArgumentsExpressEvaluator evaluator) {
        return new RedissonLockCachedPointcut(evaluator);
    }

    /** 旧版分布式锁拦截器。 */
    @Bean
    public RedissonLockInterceptor redissonLockInterceptor(
            RedissonClient redissonClient,
            RedissonLockCachedPointcut redissonLockCachedPointcut
    ) {
        return new RedissonLockInterceptor(redissonClient, redissonLockCachedPointcut);
    }

    /** 旧版分布式锁 Advisor。 */
    @Bean
    public RedissonLockAdvisor redissonLockAdvisor(RedissonLockCachedPointcut redissonLockCachedPointcut, RedissonLockInterceptor redissonLockInterceptor, RedissonAopOrderProperties redissonAopConfiguration) {
        var advisor = new RedissonLockAdvisor(redissonLockCachedPointcut);
        advisor.setAdvice(redissonLockInterceptor);
        advisor.setOrder(redissonAopConfiguration.getOrder());
        return advisor;
    }

    /** {@link io.github.opensabe.common.redisson.annotation.slock.SLock} 切点。 */
    @Bean
    public SLockPointcut sLockPointcut(MethodArgumentsExpressEvaluator evaluator) {
        return new SLockPointcut(evaluator);
    }


    /** {@link io.github.opensabe.common.redisson.annotation.slock.SLock} 拦截器。 */
    @Bean
    public SLockInterceptor sLockInterceptor(RedissonClient redissonClient, SLockPointcut pointcut) {

        return new SLockInterceptor(redissonClient, pointcut);
    }

    /** {@link io.github.opensabe.common.redisson.annotation.slock.SLock} Advisor。 */
    @Bean
    public SLockAdvisor sLockAdvisor(SLockPointcut pointcut, SLockInterceptor interceptor, RedissonAopOrderProperties configuration) {
        SLockAdvisor advisor = new SLockAdvisor(pointcut);
        advisor.setAdvice(interceptor);
        advisor.setOrder(configuration.getOrder());
        return advisor;
    }

    /** 限流切点。 */
    @Bean
    public RedissonRateLimiterCachedPointcut redissonRateLimiterCachedPointcut(MethodArgumentsExpressEvaluator evaluator) {
        return new RedissonRateLimiterCachedPointcut(evaluator);
    }

    /** 限流拦截器。 */
    @Bean
    public RedissonRateLimiterInterceptor redissonRateLimiterInterceptor(RedissonClient redissonClient, RedissonRateLimiterCachedPointcut redissonRateLimiterCachedPointcut) {
        return new RedissonRateLimiterInterceptor(redissonClient, redissonRateLimiterCachedPointcut);
    }

    /** 限流 Advisor。 */
    @Bean
    public RedissonRateLimiterAdvisor redissonRateLimiterAdvisor(RedissonRateLimiterCachedPointcut redissonRateLimiterCachedPointcut, RedissonRateLimiterInterceptor redissonRateLimiterInterceptor, RedissonAopOrderProperties redissonAopConfiguration) {
        var advisor = new RedissonRateLimiterAdvisor(redissonRateLimiterCachedPointcut);
        advisor.setAdvice(redissonRateLimiterInterceptor);
        advisor.setOrder(redissonAopConfiguration.getOrder());
        return advisor;
    }

    /** 信号量切点。 */
    @Bean
    public RedissonSemaphoreCachedPointcut redissonSemaphoreCachedPointcut(MethodArgumentsExpressEvaluator evaluator) {
        return new RedissonSemaphoreCachedPointcut(evaluator);
    }

    /** 信号量拦截器。 */
    @Bean
    public RedissonSemaphoreInterceptor redissonSemaphoreInterceptor(RedissonClient redissonClient, RedissonSemaphoreCachedPointcut redissonSemaphoreCachedPointcut) {
        return new RedissonSemaphoreInterceptor(redissonClient, redissonSemaphoreCachedPointcut);
    }

    /** 信号量 Advisor。 */
    @Bean
    public RedissonSemaphoreAdvisor redissonSemaphoreAdvisor(RedissonSemaphoreCachedPointcut redissonSemaphoreCachedPointcut, RedissonSemaphoreInterceptor redissonSemaphoreInterceptor, RedissonAopOrderProperties redissonAopConfiguration) {
        var advisor = new RedissonSemaphoreAdvisor(redissonSemaphoreCachedPointcut);
        advisor.setAdvice(redissonSemaphoreInterceptor);
        advisor.setOrder(redissonAopConfiguration.getOrder());
        return advisor;
    }


    /** 定时任务 Bean 扫描器。 */
    @Bean
    @ConditionalOnMissingBean
    public RedissonScheduledBeanPostProcessor redissonScheduledBeanPostProcessor(RedissonScheduleProperties redissonProperties) {
        return new RedissonScheduledBeanPostProcessor(redissonProperties);
    }

    /** 分布式选主定时任务监听器。 */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public RedissonScheduledListener redissonScheduledListener(RedissonScheduledBeanPostProcessor redissonScheduledBeanPostProcessor, UnifiedObservationFactory unifiedObservationFactory, RedissonClient redissonClient, MeterRegistry meterRegistry) {
        return new RedissonScheduledListener(redissonScheduledBeanPostProcessor, unifiedObservationFactory, redissonClient, meterRegistry);
    }


    /** RefreshScope 刷新后热更新定时任务参数。 */
    @Bean
    @ConditionalOnClass(name = "org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent")
    public ApplicationListener<ApplicationEvent> redissonScheduledRefreshListener(RedissonScheduledListener redissonScheduledListener, BeanFactory beanFactory) {
        return event -> {
            if (event.getClass().getName().equals("org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent")) {
                beanFactory.getBeanProvider(RedissonScheduledService.class).forEach(redissonScheduledListener::refresh);
            }
        };
    }

    /** RExpirable expire Observation → JFR。 */
    @Bean
    public RExpirableExpireObservationToJFRGenerator rExpirableExpireObservationToJFRGenerator() {
        return new RExpirableExpireObservationToJFRGenerator();
    }

    /** RLock acquired Observation → JFR。 */
    @Bean
    public RLockAcquiredObservationToJFRGenerator rLockAcquiredObservationToJFRGenerator() {
        return new RLockAcquiredObservationToJFRGenerator();
    }

    /** RLock force-release Observation → JFR。 */
    @Bean
    public RLockForceReleaseObservationToJFRGenerator rLockForceReleaseObservationToJFRGenerator() {
        return new RLockForceReleaseObservationToJFRGenerator();
    }

    /** RLock released Observation → JFR。 */
    @Bean
    public RLockReleasedObservationToJFRGenerator rLockReleasedObservationToJFRGenerator() {
        return new RLockReleasedObservationToJFRGenerator();
    }

    /** RPermitExpirableSemaphore acquired Observation → JFR。 */
    @Bean
    public RPermitSemaphoreAcquiredObservationToJFRGenerator rPermitSemaphoreAcquiredObservationToJFRGenerator() {
        return new RPermitSemaphoreAcquiredObservationToJFRGenerator();
    }

    /** RPermitExpirableSemaphore modified Observation → JFR。 */
    @Bean
    public RPermitSemaphoreModifiedObservationToJFRGenerator rPermitSemaphoreModifiedObservationToJFRGenerator() {
        return new RPermitSemaphoreModifiedObservationToJFRGenerator();
    }

    /** RPermitExpirableSemaphore released Observation → JFR。 */
    @Bean
    public RPermitSemaphoreReleasedObservationToJFRGenerator rPermitSemaphoreReleasedObservationToJFRGenerator() {
        return new RPermitSemaphoreReleasedObservationToJFRGenerator();
    }

    /** RRateLimiter acquire Observation → JFR。 */
    @Bean
    public RRateLimiterAcquireObservationToJFRGenerator rRateLimiterAcquireObservationToJFRGenerator() {
        return new RRateLimiterAcquireObservationToJFRGenerator();
    }

    /** RRateLimiter setRate Observation → JFR。 */
    @Bean
    public RRateLimiterSetRateObservationToJFRGenerator rRateLimiterSetRateObservationToJFRGenerator() {
        return new RRateLimiterSetRateObservationToJFRGenerator();
    }
}
