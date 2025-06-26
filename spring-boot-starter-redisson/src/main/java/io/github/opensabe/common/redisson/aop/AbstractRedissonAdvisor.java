package io.github.opensabe.common.redisson.aop;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.lang.NonNull;

public abstract class AbstractRedissonAdvisor<RedissonProp extends AbstractRedissonProperties> extends AbstractBeanFactoryPointcutAdvisor {
    private final AbstractRedissonCachePointcut<RedissonProp> abstractRedissonCachePointcut;

    protected AbstractRedissonAdvisor(AbstractRedissonCachePointcut<RedissonProp> abstractRedissonCachePointcut) {
        this.abstractRedissonCachePointcut = abstractRedissonCachePointcut;
    }

    @Override
    @NonNull
    public Pointcut getPointcut() {
        return abstractRedissonCachePointcut;
    }
}
