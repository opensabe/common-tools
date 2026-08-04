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

import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;

/**
 * Redisson 分布式资源名称解析的抽象属性基类。
 * <p>
 * 将 {@link #prefix} 与 SpEL 解析后的 {@link #name} 拼接为最终 Redis 键。
 */
public abstract class AbstractRedissonProperties implements RedissonNameResolver {

    /** 切点未命中时的哨兵对象。 */
    public static final Object NONE = new Object();

    /** 资源键前缀。 */
    protected final String prefix;

    /** 资源名 SpEL 模板或字面量。 */
    protected final String name;

    /** 方法参数 SpEL 求值器。 */
    protected final MethodArgumentsExpressEvaluator evaluator;

    /**
     * @param evaluator SpEL 求值器
     * @param prefix 键前缀
     * @param name 名称表达式
     */
    protected AbstractRedissonProperties(MethodArgumentsExpressEvaluator evaluator, String prefix, String name) {
        this.evaluator = evaluator;
        this.prefix = prefix;
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public String resolve(Method method, Object target, Object[] args) {
        return prefix + evaluator.resolve(method, target, args, name);
    }

}
