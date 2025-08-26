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
package io.github.opensabe.common.redisson.aop.ratelimiter;

import java.lang.reflect.Method;
import java.time.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateLimiterConfig;
import org.redisson.api.RedissonClient;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import io.github.opensabe.common.redisson.annotation.RedissonRateLimiter;
import io.github.opensabe.common.redisson.aop.AbstractRedissonProperties;
import io.github.opensabe.common.redisson.exceptions.RedissonRateLimiterException;
import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.extern.log4j.Log4j2;

/**
 * redisson 限流器核心实现类
 */
@Log4j2
public class RedissonRateLimiterInterceptor implements MethodInterceptor {

    private final RedissonClient redissonClient;
    private final RedissonRateLimiterCachedPointcut redissonRateLimiterCachedPointcut;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParserContext context = new TemplateParserContext();

    public RedissonRateLimiterInterceptor(RedissonClient redissonClient, RedissonRateLimiterCachedPointcut redissonRateLimiterCachedPointcut) {
        this.redissonClient = redissonClient;
        this.redissonRateLimiterCachedPointcut = redissonRateLimiterCachedPointcut;
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Class<?> clazz = invocation.getThis().getClass();
        RedissonRateLimiterProperties rateLimiterProperties = redissonRateLimiterCachedPointcut.getRedissonProperties(method, clazz);
        if (rateLimiterProperties == null || rateLimiterProperties == AbstractRedissonProperties.NONE) {
            log.error("RedissonRateLimiterInterceptor-invoke error! Cannot find corresponding RedissonRateLimiterProperties, method {} run without rateLimit", method.getName());
            return invocation.proceed();
        }
        RedissonRateLimiter redissonRateLimiter = rateLimiterProperties.getRedissonRateLimiter();
        String rateLimiterName = rateLimiterProperties.resolve(method, invocation.getThis(), invocation.getArguments());
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterName);
        long keepAliveTime = redissonRateLimiter.keepAlive();
        if (keepAliveTime > 0) {
            Duration kat = Duration.ofMillis(redissonRateLimiter.keepAliveTimeUnit().toMillis(redissonRateLimiter.rateInterval()));
            rateLimiter.trySetRate(redissonRateLimiter.rateType(),
                    redissonRateLimiter.rate(),
                    Duration.ofMillis(redissonRateLimiter.rateIntervalUnit().toMillis(redissonRateLimiter.rateInterval())),
                    kat
            );
        } else {
            rateLimiter.trySetRate(redissonRateLimiter.rateType(),
                    redissonRateLimiter.rate(),
                    Duration.ofMillis(redissonRateLimiter.rateIntervalUnit().toMillis(redissonRateLimiter.rateInterval()))
            );
        }
        RateLimiterConfig config = rateLimiter.getConfig();
        if (
                !config.getRate().equals(redissonRateLimiter.rate())
                        || !config.getRateInterval().equals(redissonRateLimiter.rateIntervalUnit().toMillis(redissonRateLimiter.rateInterval()))
                        || !config.getRateType().equals(redissonRateLimiter.rateType())
        ) {
            log.warn(
                    "RedissonRateLimiterInterceptor-invoke RRateLimiter config {} does not equal to current config {}, reset it. If this happens all the time, please check if you set various configuration for RRateLimiter of same name",
                    JsonUtil.toJSONString(redissonRateLimiter), JsonUtil.toJSONString(config)
            );
            rateLimiter.setRate(
                    redissonRateLimiter.rateType(),
                    redissonRateLimiter.rate(),
                    Duration.ofMillis(redissonRateLimiter.rateIntervalUnit().toMillis(redissonRateLimiter.rateInterval()))
            );
            config = rateLimiter.getConfig();
        }
        if (redissonRateLimiter.type() == RedissonRateLimiter.Type.BLOCK) {
            rateLimiter.acquire(redissonRateLimiter.permits());
        } else if (redissonRateLimiter.type() == RedissonRateLimiter.Type.TRY) {
            if (redissonRateLimiter.waitTime() < 0) {
                if (!rateLimiter.tryAcquire(redissonRateLimiter.permits())) {
                    throw new RedissonRateLimiterException("Cannot acquire permits of RRateLimiter with name: " + rateLimiterName + ", rate: " + JsonUtil.toJSONString(config));
                }
            } else {
                if (!rateLimiter.tryAcquire(redissonRateLimiter.permits(), Duration.ofMillis(redissonRateLimiter.timeUnit().toMillis(redissonRateLimiter.waitTime())))) {
                    throw new RedissonRateLimiterException("Cannot acquire permits of RRateLimiter with name: " + rateLimiterName + ", rate: " + JsonUtil.toJSONString(config));
                }
            }
        }
        return invocation.proceed();

    }
}
