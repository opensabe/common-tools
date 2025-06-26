package io.github.opensabe.common.redisson.aop.lock;

import io.github.opensabe.common.redisson.annotation.RedissonLock;
import io.github.opensabe.common.redisson.annotation.RedissonLockName;
import io.github.opensabe.common.redisson.aop.old.ExtraNamePointcut;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Method;

public class RedissonLockCachedPointcut extends ExtraNamePointcut<RedissonLockProperties> {


    public RedissonLockCachedPointcut(MethodArgumentsExpressEvaluator evaluator) {
        super(evaluator);
    }

    @SuppressWarnings("removal")
    protected RedissonLockProperties computeRedissonProperties(Method method, Class<?> clazz) {
        RedissonLock l = method.getAnnotation(RedissonLock.class);
        if (l == null) {
            l = clazz.getAnnotation(RedissonLock.class);
        }
        if (l != null) {
            Pair<RedissonLockName, Integer> pair = findParameterAnnotation(method, RedissonLockName.class);
            if (pair != null) {
                return new RedissonLockProperties(l, pair.getKey(), pair.getValue());
            }else {
                return new RedissonLockProperties(evaluator, l);
            }
        }
        return null;
    }
}
