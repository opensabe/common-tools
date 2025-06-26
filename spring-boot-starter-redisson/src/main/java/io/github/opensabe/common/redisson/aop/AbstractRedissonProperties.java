package io.github.opensabe.common.redisson.aop;

import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;

import java.lang.reflect.Method;

public abstract class AbstractRedissonProperties implements RedissonNameResolver {
    public static final Object NONE = new Object();

    protected final String prefix;

    protected final String name;

    protected final MethodArgumentsExpressEvaluator evaluator;

    protected AbstractRedissonProperties(MethodArgumentsExpressEvaluator evaluator, String prefix, String name) {
        this.evaluator = evaluator;
        this.prefix = prefix;
        this.name = name;
    }

    @Override
    public String resolve(Method method, Object target, Object[] args) {
        return prefix + evaluator.resolve(method, target, args, name);
    }

}
