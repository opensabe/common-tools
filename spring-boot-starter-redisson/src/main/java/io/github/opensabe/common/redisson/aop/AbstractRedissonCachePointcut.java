/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.redisson.aop;

import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import lombok.extern.log4j.Log4j2;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.lang.NonNull;

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
    public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass) {
        return cache.computeIfAbsent(method, k -> {
            T properties = findProperties(method, targetClass);
            if (Objects.nonNull(properties)) {
                return properties;
            }
            return AbstractRedissonProperties.NONE;
        }) != AbstractRedissonProperties.NONE;
    }

    @SuppressWarnings("unchecked")
    public T getRedissonProperties(Method method, @SuppressWarnings("unused") Class<?> targetClass) {
        return (T)cache.get(method);
    }

    @Nullable
    protected abstract T findProperties (Method method, Class<?> targetClass);
}
