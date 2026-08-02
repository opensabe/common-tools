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

/**
 * {@link RedissonSemaphore} 注解运行时属性。
 */
public class RedissonSemaphoreProperties extends ExtraNameProperties {

    /** 原始信号量注解。 */
    @Getter
    private final RedissonSemaphore redissonSemaphore;

    /**
     * 通过已废弃的 {@link RedissonSemaphoreName} 参数解析名称。
     *
     * @param redissonSemaphore 信号量注解
     * @param redissonSemaphoreName 参数名称注解
     * @param parameterIndex 参数索引
     */
    public RedissonSemaphoreProperties(RedissonSemaphore redissonSemaphore, RedissonSemaphoreName redissonSemaphoreName, int parameterIndex) {
        super(redissonSemaphoreName.prefix(), redissonSemaphore.name(), parameterIndex, redissonSemaphoreName.expression());
        this.redissonSemaphore = redissonSemaphore;
    }

    /**
     * 通过注解 {@link RedissonSemaphore#name()} SpEL 解析名称。
     *
     * @param evaluator SpEL 求值器
     * @param redissonSemaphore 信号量注解
     */
    public RedissonSemaphoreProperties(MethodArgumentsExpressEvaluator evaluator, RedissonSemaphore redissonSemaphore) {
        super(evaluator, redissonSemaphore.prefix(), redissonSemaphore.name());
        this.redissonSemaphore = redissonSemaphore;
    }
}
