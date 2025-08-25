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
package io.github.opensabe.common.redisson.aop.semaphore;

import io.github.opensabe.common.redisson.annotation.RedissonSemaphore;
import io.github.opensabe.common.redisson.annotation.RedissonSemaphoreName;
import io.github.opensabe.common.redisson.aop.old.ExtraNameProperties;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import lombok.Getter;

public class RedissonSemaphoreProperties extends ExtraNameProperties {
    @Getter
    private final RedissonSemaphore redissonSemaphore;

    public RedissonSemaphoreProperties(RedissonSemaphore redissonSemaphore, RedissonSemaphoreName redissonSemaphoreName, int parameterIndex) {
        super(redissonSemaphoreName.prefix(), redissonSemaphore.name(), parameterIndex, redissonSemaphoreName.expression());
        this.redissonSemaphore = redissonSemaphore;
    }
    public RedissonSemaphoreProperties(MethodArgumentsExpressEvaluator evaluator, RedissonSemaphore redissonSemaphore) {
        super(evaluator, redissonSemaphore.prefix(), redissonSemaphore.name());
        this.redissonSemaphore = redissonSemaphore;
    }
}
