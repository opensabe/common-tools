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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.lang.NonNull;

import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import lombok.extern.log4j.Log4j2;

/**
 * 带方法级缓存的 Redisson 切点抽象基类。
 * <p>
 * 首次匹配时将 {@link AbstractRedissonProperties} 解析结果缓存在 {@link #cache} 中。
 *
 * @param <T> 注解属性类型
 */
@Log4j2
public abstract class AbstractRedissonCachePointcut<T extends AbstractRedissonProperties> extends StaticMethodMatcherPointcut {

    /** SpEL 锁名解析器。 */
    protected final MethodArgumentsExpressEvaluator evaluator;

    /** 方法 → 解析后的 Redisson 属性（或 {@link AbstractRedissonProperties#NONE} 哨兵）。 */
    private final Map<Method, Object> cache = new ConcurrentHashMap<>();

    /**
     * @param evaluator 方法参数 SpEL 求值器
     */
    protected AbstractRedissonCachePointcut(MethodArgumentsExpressEvaluator evaluator) {
        this.evaluator = evaluator;
    }


    /** {@inheritDoc} — 命中带 Redisson 注解的方法时返回 {@code true}。 */
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

    /**
     * 获取已缓存的方法级 Redisson 属性。
     *
     * @param method 目标方法
     * @param targetClass 目标类（未使用，保留以兼容子类）
     * @return 属性实例，未命中时为 {@code null}
     */
    @SuppressWarnings("unchecked")
    public T getRedissonProperties(Method method, @SuppressWarnings("unused") Class<?> targetClass) {
        return (T) cache.get(method);
    }

    /**
     * 从方法或类上查找 Redisson 注解并构建属性对象。
     *
     * @param method 目标方法
     * @param targetClass 目标类
     * @return 属性实例，无注解时返回 {@code null}
     */
    @Nullable
    protected abstract T findProperties(Method method, Class<?> targetClass);
}
