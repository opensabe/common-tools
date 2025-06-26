package io.github.opensabe.common.redisson.aop.semaphore;

import io.github.opensabe.common.redisson.annotation.RedissonSemaphore;
import io.github.opensabe.common.redisson.annotation.RedissonSemaphoreName;
import io.github.opensabe.common.redisson.aop.old.ExtraNamePointcut;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class RedissonSemaphoreCachedPointcut extends ExtraNamePointcut<RedissonSemaphoreProperties> {

    public RedissonSemaphoreCachedPointcut(MethodArgumentsExpressEvaluator evaluator) {
        super(evaluator);
    }

    @Override
    protected RedissonSemaphoreProperties computeRedissonProperties(Method method, Class<?> clazz) {
        try {
            Method m = clazz.getMethod(method.getName(), method.getParameterTypes());
            RedissonSemaphore l = m.getAnnotation(RedissonSemaphore.class);
            if (l == null) {
                l = clazz.getAnnotation(RedissonSemaphore.class);
            }
            if (l != null) {
                Pair<RedissonSemaphoreName, Integer> pair = findParameterAnnotation(method, RedissonSemaphoreName.class);
                if (pair == null) {
                    return new RedissonSemaphoreProperties(evaluator, l);
                }else {
                    return new RedissonSemaphoreProperties(l, pair.getKey(), pair.getValue());
                }
            }
        } catch (NoSuchMethodException e) {
        }
        return null;
    }
}
