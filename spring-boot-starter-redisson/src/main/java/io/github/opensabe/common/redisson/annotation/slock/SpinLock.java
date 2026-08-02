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
package io.github.opensabe.common.redisson.annotation.slock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AliasFor;

/**
 * 声明 Redisson 自旋锁（{@link org.redisson.api.RedissonClient#getSpinLock(String, LockOptions.BackOff)}）。
 */
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SLock(name = "", lockFeature = SLock.LockFeature.SPIN)
public @interface SpinLock {

    /**
     * 锁的名称表达式
     *
     * @see Cacheable#cacheNames()
     */
    @AliasFor(annotation = SLock.class)
    String[] name();

    /** 锁键前缀。 */
    @AliasFor(annotation = SLock.class)
    String prefix() default RedissonLock.DEFAULT_PREFIX;

    /**
     * 锁等待时间
     */
    @AliasFor(annotation = SLock.class)
    long waitTime() default 1000;

    /**
     * 锁最长持有时间
     */
    @AliasFor(annotation = SLock.class)
    long leaseTime() default -1;

    /**
     * 时间单位
     */
    @AliasFor(annotation = SLock.class)
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /** 加锁策略。 */
    @AliasFor(annotation = SLock.class)
    SLock.LockType lockType() default SLock.LockType.BLOCK_LOCK;

    /** 自旋退避算法。 */
    @AliasFor(annotation = SLock.class)
    SLock.BackOffType backOffType() default SLock.BackOffType.EXPONENTIAL;

    /** 固定退避间隔（毫秒）。 */
    @AliasFor(annotation = SLock.class)
    long backOffDelay() default 64L;

    /** 指数退避最大间隔（毫秒）。 */
    @AliasFor(annotation = SLock.class)
    long backOffMaxDelay() default 128;

    /** 指数退避初始间隔（毫秒）。 */
    @AliasFor(annotation = SLock.class)
    long backOffInitialDelay() default 1;

    /** 指数退避乘数。 */
    @AliasFor(annotation = SLock.class)
    int backOffMultiplier() default 2;


}
