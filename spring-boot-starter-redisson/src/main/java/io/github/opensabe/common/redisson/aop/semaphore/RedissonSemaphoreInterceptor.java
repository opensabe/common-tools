package io.github.opensabe.common.redisson.aop.semaphore;

import io.github.opensabe.common.redisson.annotation.RedissonSemaphore;
import io.github.opensabe.common.redisson.annotation.RedissonSemaphoreName;
import io.github.opensabe.common.redisson.aop.AbstractRedissonProperties;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.exceptions.RedissonSemaphoreException;
import lombok.extern.log4j.Log4j2;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * redisson 限流器核心实现类
 */
@Log4j2
public class RedissonSemaphoreInterceptor implements MethodInterceptor {

    private final RedissonClient redissonClient;
    private final RedissonSemaphoreCachedPointcut redissonSemaphoreCachedPointcut;
    private final UnifiedObservationFactory unifiedObservationFactory;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParserContext context = new TemplateParserContext();

    public RedissonSemaphoreInterceptor(RedissonClient redissonClient, RedissonSemaphoreCachedPointcut redissonSemaphoreCachedPointcut, UnifiedObservationFactory unifiedObservationFactory) {
        this.redissonClient = redissonClient;
        this.redissonSemaphoreCachedPointcut = redissonSemaphoreCachedPointcut;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Class<?> clazz = invocation.getThis().getClass();
        RedissonSemaphoreProperties redissonSemaphoreProperties = redissonSemaphoreCachedPointcut.getRedissonProperties(method, clazz);
        if (redissonSemaphoreProperties == null || redissonSemaphoreProperties == AbstractRedissonProperties.NONE) {
            log.error("RedissonRateLimiterInterceptor-invoke error! Cannot find corresponding RedissonSemaphoreProperties, method {} run without semaphore", method.getName());
            return invocation.proceed();
        }
        RedissonSemaphore redissonSemaphore = redissonSemaphoreProperties.getRedissonSemaphore();
        String semaphoreName = getSemaphoreName(redissonSemaphoreProperties, invocation.getArguments());
        redissonSemaphoreProperties.setSemaphoreName(semaphoreName);
        RPermitExpirableSemaphore semaphore = redissonClient.getPermitExpirableSemaphore(semaphoreName);
        //首先尝试设置总的 permits
        int totalPermits = redissonSemaphore.totalPermits();
        boolean trySetPermits = semaphore.trySetPermits(totalPermits);
        if (trySetPermits) {
            log.info("RedissonSemaphoreInterceptor-invoke trySetPermits {} success", totalPermits);
        } else {
            log.info("RedissonSemaphoreInterceptor-invoke trySetPermits {} return false, semaphore total permits has been set", totalPermits);
        }

        RedissonSemaphore.Type type = redissonSemaphore.type();
        TimeUnit timeUnit = redissonSemaphore.timeUnit();
        String acquired = null;
        try {
            if (type == RedissonSemaphore.Type.BLOCK) {
                acquired = semaphore.acquire(redissonSemaphore.leaseTime(), timeUnit);
                log.info("RedissonSemaphoreInterceptor-invoke block acquired: {}", acquired);

            } else if (type == RedissonSemaphore.Type.TRY) {
                long waitTime = redissonSemaphore.waitTime();
                if (waitTime < 0) {
                    acquired = semaphore.tryAcquire();
                    if (acquired == null) {
                        throw new RedissonSemaphoreException("Cannot acquire permit of semaphore with name: " + semaphoreName);
                    }
                } else {
                    acquired = semaphore.tryAcquire(waitTime, timeUnit);
                    if (acquired == null) {
                        throw new RedissonSemaphoreException("Cannot acquire permit of semaphore with name: " + semaphoreName);
                    }
                }
            }
            return invocation.proceed();
        } finally {
            if (acquired != null) {
                semaphore.release(acquired);
                log.info("RedissonSemaphoreInterceptor-invoke block released: {}", acquired);
            }
        }
    }

    private String getSemaphoreName(RedissonSemaphoreProperties redissonSemaphoreProperties, Object... params) {
        StringBuilder lockName = new StringBuilder();
        RedissonSemaphoreName redissonSemaphoreName = redissonSemaphoreProperties.getRedissonSemaphoreName();
        if (redissonSemaphoreName != null) {
            int parameterIndex = redissonSemaphoreProperties.getParameterIndex();
            String prefix = redissonSemaphoreName.prefix();
            String expression = redissonSemaphoreName.expression();
            if (StringUtils.isNotBlank(expression)) {
                lockName.append(prefix).append(parser.parseExpression(expression, context).getValue(params[parameterIndex]));
            } else {
                lockName.append(prefix).append(params[parameterIndex]);
            }
        } else {
            lockName.append(redissonSemaphoreProperties.getRedissonSemaphore().name());
        }
        return lockName.toString();
    }
}
