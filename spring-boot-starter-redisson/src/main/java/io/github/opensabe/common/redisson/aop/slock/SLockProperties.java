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
 * @author heng.ma
 */
public class SLockProperties extends AbstractRedissonProperties {

    @Getter
    private final SLock lock;

    public SLockProperties(MethodArgumentsExpressEvaluator evaluator, SLock lock) {
        super(evaluator, null, null);
        this.lock = lock;
    }

    @Override
    public String resolve(Method method, Object target, Object[] args) {
        throw new UnsupportedOperationException("SLockProperties does not support resolve method");
    }

    public MethodArgumentsExpressEvaluator evaluator() {
        return evaluator;
    }
}
