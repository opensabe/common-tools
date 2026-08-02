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

/**
 * 将方法上下文解析为 Redisson 资源键（锁名、限流器名等）。
 */
public interface RedissonNameResolver {

    /**
     * 解析最终 Redis 键。
     *
     * @param method 被拦截方法
     * @param target 目标对象
     * @param args 方法参数
     * @return 完整键名（含前缀）
     */
    String resolve(Method method, Object target, Object[] args);
}
