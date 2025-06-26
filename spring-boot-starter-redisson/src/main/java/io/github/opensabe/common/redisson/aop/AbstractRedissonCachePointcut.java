package io.github.opensabe.common.redisson.aop;

import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import lombok.extern.log4j.Log4j2;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public abstract class AbstractRedissonCachePointcut<T extends AbstractRedissonProperties> extends StaticMethodMatcherPointcut {
    /**
     * Key 为方法全限定名称 + 参数，value 为对应的 Redisson 锁注解以及锁名称
     */
    private final Map<Method, Object> cache = new ConcurrentHashMap<>();

    protected final MethodArgumentsExpressEvaluator evaluator;

    protected AbstractRedissonCachePointcut(MethodArgumentsExpressEvaluator evaluator) {
        this.evaluator = evaluator;
    }


    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return cache.computeIfAbsent(method, k -> {
            T properties = findProperties(method, targetClass);
            if (Objects.nonNull(properties)) {
                return properties;
            }
            return AbstractRedissonProperties.NONE;
        }) != AbstractRedissonProperties.NONE;
    }

    public T getRedissonProperties(Method method, Class<?> targetClass) {
        Object o = cache.get(method);
        return (T) o;
    }

    @Nullable
    protected abstract T findProperties (Method method, Class<?> targetClass);
}
