package io.github.opensabe.common.redisson.aop.slock;

import io.github.opensabe.common.redisson.annotation.slock.SLock;
import io.github.opensabe.common.redisson.aop.AbstractRedissonProperties;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import lombok.Getter;

import java.lang.reflect.Method;

/**
 * @author heng.ma
 */
public class SLockProperties extends AbstractRedissonProperties {

    @Getter
    private final SLock lock;

    public SLockProperties(MethodArgumentsExpressEvaluator evaluator, SLock lock) {
        super(evaluator, null, null);
        this.lock = lock;
    }

    @Override
    public String resolve(Method method, Object target, Object[] args) {
        throw new UnsupportedOperationException("SLockProperties does not support resolve method");
    }

    public MethodArgumentsExpressEvaluator evaluator() {
        return evaluator;
    }
}
