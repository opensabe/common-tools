package io.github.opensabe.common.redisson.aop;

import java.lang.reflect.Method;

public interface RedissonNameResolver {

    String resolve(Method method, Object target, Object[] args);
}
