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
package io.github.opensabe.common.redisson.aop.old;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.Pair;

import io.github.opensabe.common.redisson.aop.AbstractRedissonCachePointcut;
import io.github.opensabe.common.redisson.aop.AbstractRedissonProperties;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;

/**
 * 支持参数级 {@code *Name} 注解的 Redisson 切点基类（旧版 API）。
 * <p>
 * 在方法、类及父类/接口上查找注解，并解析参数上的动态名称注解。
 *
 * @param <T> 属性类型
 */
public abstract class ExtraNamePointcut<T extends AbstractRedissonProperties> extends AbstractRedissonCachePointcut<T> {


    /**
     * @param evaluator SpEL 求值器
     */
    protected ExtraNamePointcut(MethodArgumentsExpressEvaluator evaluator) {
        super(evaluator);
    }

    /**
     * 查找方法参数上第一个指定类型的名称注解。
     *
     * @param method 目标方法
     * @param annotationClass 名称注解类型
     * @param <A> 注解类型
     * @return 注解实例与参数索引；未找到返回 {@code null}
     */
    protected static <A extends Annotation> Pair<A, Integer> findParameterAnnotation(Method method, Class<A> annotationClass) {
        Annotation[][] as = method.getParameterAnnotations();
        for (int i = 0; i < as.length; i++) {
            Annotation[] ar = as[i];
            if (ArrayUtils.isEmpty(ar)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Optional<A> op = Arrays.stream(ar)
                    .filter(annotationClass::isInstance)
                    .map(a -> (A) a)
                    .findFirst();
            if (op.isPresent()) {
                return Pair.of(op.get(), i);
            }
        }
        return null;
    }

    /** {@inheritDoc} — 沿类继承链与接口链查找注解。 */
    @Override
    protected T findProperties(Method method, Class<?> targetClass) {
        T redissonProp = computeRedissonProperties(method, targetClass);
        if (redissonProp != null) {
            return redissonProp;
        }
        List<Class<?>> allSuperclasses = ClassUtils.getAllSuperclasses(targetClass);
        Optional<T> optional = fromClasses(allSuperclasses, method);
        if (optional.isEmpty()) {
            allSuperclasses = ClassUtils.getAllInterfaces(targetClass);
            optional = fromClasses(allSuperclasses, method);
        }
        return optional.orElse(null);
    }

    /**
     * 沿父类/接口列表查找首个匹配的 Redisson 属性。
     *
     * @param list 待搜索的类型列表
     * @param method 原始方法（用于反射同名方法）
     * @return 首个非空属性
     */
    private Optional<T> fromClasses(List<Class<?>> list, Method method) {
        return list.stream()
                .map(i -> {
                    try {
                        return computeRedissonProperties(i.getMethod(method.getName(), method.getParameterTypes()), i);
                    } catch (NoSuchMethodException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst();
    }

    /**
     * 从方法或类注解构建 Redisson 属性。
     *
     * @param method 目标方法
     * @param clazz 声明类
     * @return 属性实例，无注解时 {@code null}
     */
    protected abstract T computeRedissonProperties(Method method, Class<?> clazz);
}
