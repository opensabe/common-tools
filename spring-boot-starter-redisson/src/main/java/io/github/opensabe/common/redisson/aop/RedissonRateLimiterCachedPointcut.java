package io.github.opensabe.common.redisson.aop;

import io.github.opensabe.common.redisson.annotation.RedissonRateLimiter;
import io.github.opensabe.common.redisson.annotation.RedissonRateLimiterName;
import io.micrometer.core.instrument.util.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class RedissonRateLimiterCachedPointcut extends AbstractRedissonCachePointcut<RedissonRateLimiterProperties> {
    @Override
    protected RedissonRateLimiterProperties computeRedissonProperties(Method method, Class<?> clazz) {
        try {
            Method m = clazz.getMethod(method.getName(), method.getParameterTypes());
            RedissonRateLimiter l = m.getAnnotation(RedissonRateLimiter.class);
            if (l == null) {
                l = clazz.getAnnotation(RedissonRateLimiter.class);
            }
            if (l != null) {
                Annotation[][] as = method.getParameterAnnotations();
                for (int i = 0; i < as.length; i++) {
                    Annotation[] ar = as[i];
                    if (ArrayUtils.isEmpty(ar)) {
                        continue;
                    }
                    //获取第一个 RedissonLockName 注解的参数
                    Optional<RedissonRateLimiterName> op = Arrays.stream(ar)
                            .filter(a -> a instanceof RedissonRateLimiterName)
                            .map(a -> (RedissonRateLimiterName) a)
                            .findFirst();
                    if (op.isPresent()) {
                        return new RedissonRateLimiterProperties(l, op.get(), i);
                    }
                }
                if (StringUtils.isNotBlank(l.name())) {
                    return new RedissonRateLimiterProperties(l, null, -1);
                }
            }
        } catch (NoSuchMethodException e) {
        }
        return null;
    }
}
