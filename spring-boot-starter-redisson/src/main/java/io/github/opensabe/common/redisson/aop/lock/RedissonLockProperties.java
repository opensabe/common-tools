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
package io.github.opensabe.common.redisson.aop.lock;

import io.github.opensabe.common.redisson.annotation.RedissonLock;
import io.github.opensabe.common.redisson.aop.old.ExtraNameProperties;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;


/**
 * 旧版 {@link io.github.opensabe.common.redisson.annotation.RedissonLock} 注解运行时属性。
 */
public class RedissonLockProperties extends ExtraNameProperties {

    /** 原始锁注解。 */
    private final RedissonLock redissonLock;

    /**
     * 通过注解 {@link #name()} SpEL 解析锁名。
     *
     * @param evaluator SpEL 求值器
     * @param redissonLock 锁注解
     */
    public RedissonLockProperties(MethodArgumentsExpressEvaluator evaluator, RedissonLock redissonLock) {
        super(evaluator, redissonLock.prefix(), redissonLock.name());
        this.redissonLock = redissonLock;
    }

    /**
     * 通过已废弃的 {@link io.github.opensabe.common.redisson.annotation.RedissonLockName} 参数解析锁名。
     *
     * @param redissonLock 锁注解
     * @param redissonLockName 参数上的名称注解
     * @param pamaterIndex 参数索引
     */
    @SuppressWarnings("removal")
    public RedissonLockProperties(RedissonLock redissonLock,
                                  io.github.opensabe.common.redisson.annotation.RedissonLockName redissonLockName,
                                  int pamaterIndex) {
        super(redissonLockName.prefix(), redissonLock.name(), pamaterIndex, redissonLockName.expression());
        this.redissonLock = redissonLock;
    }

    /**
     * @return 锁注解实例
     */
    public RedissonLock getRedissonLock() {
        return redissonLock;
    }
}
