package io.github.opensabe.common.redisson.aop.semaphore;

import io.github.opensabe.common.redisson.annotation.RedissonSemaphore;
import io.github.opensabe.common.redisson.aop.old.ExtraNamePointcut;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Method;

public class RedissonSemaphoreCachedPointcut extends ExtraNamePointcut<RedissonSemaphoreProperties> {

    public RedissonSemaphoreCachedPointcut(MethodArgumentsExpressEvaluator evaluator) {
        super(evaluator);
    }

    @Override
    @SuppressWarnings("removal")
    protected RedissonSemaphoreProperties computeRedissonProperties(Method method, Class<?> clazz) {
        RedissonSemaphore l = method.getAnnotation(RedissonSemaphore.class);
        if (l == null) {
            l = clazz.getAnnotation(RedissonSemaphore.class);
        }
        if (l != null) {
            Pair<io.github.opensabe.common.redisson.annotation.RedissonSemaphoreName, Integer> pair =
                    findParameterAnnotation(method, io.github.opensabe.common.redisson.annotation.RedissonSemaphoreName.class);
            if (pair == null) {
                return new RedissonSemaphoreProperties(evaluator, l);
            }else {
                return new RedissonSemaphoreProperties(l, pair.getKey(), pair.getValue());
            }
        }
        return null;
    }
}
