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
package io.github.opensabe.common.redisson.annotation;


import io.github.opensabe.common.redisson.annotation.slock.RedissonLock;

import java.lang.annotation.*;

/**
 * 分布式锁名称注解
 * @deprecated use {@link io.github.opensabe.common.redisson.annotation.slock.RedissonLock} instead
 */
@Deprecated(forRemoval = true, since = "2.0.0")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface RedissonLockName {


    String prefix() default RedissonLock.DEFAULT_PREFIX;

    String expression() default "";
}
