package io.github.opensabe.common.redisson.aop.ratelimiter;

import io.github.opensabe.common.redisson.annotation.RedissonRateLimiter;
import io.github.opensabe.common.redisson.annotation.RedissonRateLimiterName;
import io.github.opensabe.common.redisson.aop.old.ExtraNamePointcut;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Method;

public class RedissonRateLimiterCachedPointcut extends ExtraNamePointcut<RedissonRateLimiterProperties> {


    public RedissonRateLimiterCachedPointcut(MethodArgumentsExpressEvaluator evaluator) {
        super(evaluator);
    }

    @Override
    @SuppressWarnings("removal")
    protected RedissonRateLimiterProperties computeRedissonProperties(Method method, Class<?> clazz) {
        RedissonRateLimiter l = method.getAnnotation(RedissonRateLimiter.class);
        if (l == null) {
            l = clazz.getAnnotation(RedissonRateLimiter.class);
        }
        if (l != null) {
            Pair<RedissonRateLimiterName, Integer> pair = findParameterAnnotation(method, RedissonRateLimiterName.class);
            if (pair != null) {
                return new RedissonRateLimiterProperties(l, pair.getKey(), pair.getValue());
            }else {
                return new RedissonRateLimiterProperties(evaluator, l);
            }
        }

        return null;
    }
}
