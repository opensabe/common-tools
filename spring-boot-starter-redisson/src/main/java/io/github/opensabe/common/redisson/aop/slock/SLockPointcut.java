package io.github.opensabe.common.redisson.aop.slock;

import io.github.opensabe.common.redisson.annotation.slock.SLock;
import io.github.opensabe.common.redisson.aop.AbstractRedissonCachePointcut;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author heng.ma
 */
public class SLockPointcut extends AbstractRedissonCachePointcut<SLockProperties> {


    public SLockPointcut(MethodArgumentsExpressEvaluator evaluator) {
        super(evaluator);
    }

    @Nullable
    @Override
    protected SLockProperties findProperties(Method method, Class<?> targetClass) {
        SLock lock = AnnotatedElementUtils.findMergedAnnotation(method, SLock.class);

        if (Objects.isNull(lock)) {
            lock = AnnotatedElementUtils.findMergedAnnotation(targetClass, SLock.class);
        }
        if (Objects.isNull(lock)) {
            return null;
        }
        return new SLockProperties(evaluator, lock);
    }
}
