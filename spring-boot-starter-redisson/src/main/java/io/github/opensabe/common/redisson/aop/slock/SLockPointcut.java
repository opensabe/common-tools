package io.github.opensabe.common.redisson.aop.slock;

import io.github.opensabe.common.redisson.annotation.slock.SLock;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author heng.ma
 */
public class SLockPointcut extends StaticMethodMatcherPointcut {

    private final Map<Method, SLock> cache = new HashMap<>();

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return Objects.nonNull(findSLock(method, targetClass));
    }

    SLock findSLock (Method method, Class<?> clazz) {
        return cache.computeIfAbsent(method, k -> {
            SLock lock = AnnotatedElementUtils.findMergedAnnotation(method, SLock.class);

            if (Objects.isNull(lock)) {
                lock = AnnotatedElementUtils.findMergedAnnotation(clazz, SLock.class);
            }
            return lock;
        });
    }
}
