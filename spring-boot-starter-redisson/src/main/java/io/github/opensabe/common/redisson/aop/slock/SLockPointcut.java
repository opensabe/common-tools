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
import java.util.Objects;

import javax.annotation.Nullable;

import org.springframework.core.annotation.AnnotatedElementUtils;

import io.github.opensabe.common.redisson.annotation.slock.SLock;
import io.github.opensabe.common.redisson.aop.AbstractRedissonCachePointcut;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;

/**
 * {@link SLock} 切点，通过 {@link org.springframework.core.annotation.AnnotatedElementUtils} 合并查找注解。
 */
public class SLockPointcut extends AbstractRedissonCachePointcut<SLockProperties> {


    /**
     * @param evaluator 方法参数 SpEL 求值器
     */
    public SLockPointcut(MethodArgumentsExpressEvaluator evaluator) {
        super(evaluator);
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    protected SLockProperties findProperties(Method method, Class<?> targetClass) {
        SLock lock = AnnotatedElementUtils.findMergedAnnotation(method, SLock.class);

        if (Objects.isNull(lock)) {
            lock = AnnotatedElementUtils.findMergedAnnotation(targetClass, SLock.class);
        }
        if (Objects.isNull(lock)) {
            return null;
        }
        return new SLockProperties(evaluator, lock);
    }
}
