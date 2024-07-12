package io.github.opensabe.common.redisson.aop;

import io.github.opensabe.common.redisson.annotation.RedissonSemaphore;
import io.github.opensabe.common.redisson.annotation.RedissonSemaphoreName;
import io.micrometer.core.instrument.util.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class RedissonSemaphoreCachedPointcut extends AbstractRedissonCachePointcut<RedissonSemaphoreProperties> {
    @Override
    protected RedissonSemaphoreProperties computeRedissonProperties(Method method, Class<?> clazz) {
        try {
            Method m = clazz.getMethod(method.getName(), method.getParameterTypes());
            RedissonSemaphore l = m.getAnnotation(RedissonSemaphore.class);
            if (l == null) {
                l = clazz.getAnnotation(RedissonSemaphore.class);
            }
            if (l != null) {
                Annotation[][] as = method.getParameterAnnotations();
                for (int i = 0; i < as.length; i++) {
                    Annotation[] ar = as[i];
                    if (ArrayUtils.isEmpty(ar)) {
                        continue;
                    }
                    //获取第一个 RedissonLockName 注解的参数
                    Optional<RedissonSemaphoreName> op = Arrays.stream(ar)
                            .filter(a -> a instanceof RedissonSemaphoreName)
                            .map(a -> (RedissonSemaphoreName) a)
                            .findFirst();
                    if (op.isPresent()) {
                        return new RedissonSemaphoreProperties(l, op.get(), i);
                    }
                }
                if (StringUtils.isNotBlank(l.name())) {
                    return new RedissonSemaphoreProperties(l, null, -1);
                }
            }
        } catch (NoSuchMethodException e) {
        }
        return null;
    }
}
