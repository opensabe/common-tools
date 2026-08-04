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
package io.github.opensabe.common.redisson.aop.slock;

import java.lang.reflect.Method;

import io.github.opensabe.common.redisson.annotation.slock.SLock;
import io.github.opensabe.common.redisson.aop.AbstractRedissonProperties;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import lombok.Getter;

/**
 * {@link SLock} 注解运行时属性；锁名由 {@link #lock()} 的 {@link SLock#name()} 数组逐项解析。
 */
public class SLockProperties extends AbstractRedissonProperties {

    /** 原始 SLock 注解（含组合注解元数据）。 */
    @Getter
    private final SLock lock;

    /**
     * @param evaluator SpEL 求值器
     * @param lock 合并后的 SLock 注解
     */
    public SLockProperties(MethodArgumentsExpressEvaluator evaluator, SLock lock) {
        super(evaluator, null, null);
        this.lock = lock;
    }

    /**
     * 不支持统一 resolve；多锁名在 {@link SLockInterceptor} 中逐项解析。
     *
     * @throws UnsupportedOperationException 始终抛出
     */
    @Override
    public String resolve(Method method, Object target, Object[] args) {
        throw new UnsupportedOperationException("SLockProperties does not support resolve method");
    }

    /**
     * @return SpEL 求值器
     */
    public MethodArgumentsExpressEvaluator evaluator() {
        return evaluator;
    }
}
