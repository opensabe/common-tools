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

import org.redisson.api.LockOptions;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.Cacheable;

import io.github.opensabe.common.redisson.exceptions.RedissonLockException;

/**
 * 组合式分布式锁元注解，承载锁名称、特性与加锁策略。
 * <p>
 * 通常通过 {@link RedissonLock}、{@link FairLock} 等组合注解间接使用，
 * 由 {@link io.github.opensabe.common.redisson.aop.slock.SLockInterceptor} 拦截执行。
 */
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SLock {

    /**
     * 锁名称 SpEL 表达式数组；支持多锁组合。
     *
     * @see Cacheable#cacheNames()
     */
    String[] name();

    /** 锁键前缀，默认 {@link RedissonLock#DEFAULT_PREFIX}。 */
    String prefix() default RedissonLock.DEFAULT_PREFIX;

    /** tryLock 最长等待时间。 */
    long waitTime() default 1000;

    /** 锁 lease 时间；-1 表示看门狗续期。 */
    long leaseTime() default -1;

    /** {@link #waitTime()} 与 {@link #leaseTime()} 的时间单位。 */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /** 加锁策略。 */
    LockType lockType() default LockType.BLOCK_LOCK;

    /** Redisson 锁实现特性。 */
    LockFeature lockFeature() default LockFeature.DEFAULT;

    /** 自旋锁退避策略，仅 {@link LockFeature#SPIN} 生效。 */
    BackOffType backOffType() default BackOffType.EXPONENTIAL;

    /** 固定退避间隔（毫秒），{@link LockFeature#SPIN} + {@link BackOffType#CONSTANT}。 */
    long backOffDelay() default 64L;

    /** 指数退避最大间隔（毫秒）。 */
    long backOffMaxDelay() default 128;

    /** 指数退避初始间隔（毫秒）。 */
    long backOffInitialDelay() default 1;

    /** 指数退避乘数。 */
    int backOffMultiplier() default 2;

    /** 读写锁模式，仅 {@link LockFeature#READ_WRITE} 生效。 */
    ReadOrWrite readOrWrite() default ReadOrWrite.READ;

    /** 加锁行为。 */
    enum LockType {

        /** 阻塞加锁。 @see org.redisson.api.RLock#lock(long, TimeUnit) */
        BLOCK_LOCK {
            @Override
            public boolean lock(SLock content, RLock lock) {
                lock.lock(content.leaseTime(), content.timeUnit());
                return true;
            }
        },
        /** tryLock 不等待。 @see org.redisson.api.RLock#tryLock() */
        TRY_LOCK_NOWAIT {
            @Override
            public boolean lock(SLock content, RLock lock) {
                return lock.tryLock();
            }
        },
        /** tryLock 带等待。 @see org.redisson.api.RLock#tryLock(long, long, TimeUnit) */
        TRY_LOCK {
            @Override
            public boolean lock(SLock content, RLock lock) {
                try {
                    return lock.tryLock(content.waitTime(), content.leaseTime(), content.timeUnit());
                } catch (InterruptedException e) {
                    throw new RedissonLockException("can not get redisson lock", e);
                }
            }
        };

        /**
         * 执行加锁逻辑。
         *
         * @param content 注解实例
         * @param lock 锁对象
         * @return 是否成功加锁
         */
        public abstract boolean lock(SLock content, RLock lock);
    }

    /** Redisson 锁实现类型。 */
    enum LockFeature {
        /** 可重入锁。 */
        DEFAULT {
            @Override
            public RLock getLock(String name, SLock content, RedissonClient redissonClient) {
                return redissonClient.getLock(name);
            }
        },
        /** 公平锁。 */
        FAIR {
            @Override
            public RLock getLock(String name, SLock content, RedissonClient redissonClient) {
                return redissonClient.getFairLock(name);
            }
        },
        /** 自旋锁。 */
        SPIN {
            @Override
            public RLock getLock(String name, SLock content, RedissonClient redissonClient) {
                return redissonClient.getSpinLock(name, content.backOffType().backOff(content));
            }
        },
        /** 读写锁。 */
        READ_WRITE {
            @Override
            public RLock getLock(String name, SLock content, RedissonClient redissonClient) {
                return content.readOrWrite().transform(redissonClient.getReadWriteLock(name));
            }
        },

        /** 栅栏锁。 */
        FENCED {
            @Override
            public RLock getLock(String name, SLock content, RedissonClient redissonClient) {
                return redissonClient.getFencedLock(name);
            }
        };

        /**
         * 按锁特性从 {@link RedissonClient} 获取 {@link RLock}。
         *
         * @param name 已解析锁键
         * @param content 注解实例
         * @param redissonClient 客户端
         * @return 锁对象
         */
        public abstract RLock getLock(String name, SLock content, RedissonClient redissonClient);
    }


    /** 自旋锁退避算法。 */
    enum BackOffType {
        /** 固定间隔。 @see LockOptions.ConstantBackOff */
        CONSTANT {
            @Override
            LockOptions.BackOff backOff(SLock content) {
                return new LockOptions.ConstantBackOff()
                        .delay(content.backOffDelay());
            }
        },
        /** 指数退避。 @see LockOptions.ExponentialBackOff */
        EXPONENTIAL {
            @Override
            LockOptions.BackOff backOff(SLock content) {
                return new LockOptions.ExponentialBackOff()
                        .initialDelay(content.backOffInitialDelay())
                        .maxDelay(content.backOffMaxDelay())
                        .multiplier(content.backOffMultiplier());
            }
        },
        ;

        /**
         * 构建 Redisson 退避配置。
         *
         * @param content 注解实例
         * @return 退避策略
         */
        abstract LockOptions.BackOff backOff(SLock content);
    }

    /** 读写锁子锁类型。 */
    enum ReadOrWrite {
        /** 读锁。 @see org.redisson.api.RReadWriteLock#readLock() */
        READ {
            @Override
            RLock transform(RReadWriteLock lock) {
                return lock.readLock();
            }
        },

        /** 写锁。 @see org.redisson.api.RReadWriteLock#writeLock() */
        WRITE {
            @Override
            RLock transform(RReadWriteLock lock) {
                return lock.writeLock();
            }
        },
        ;

        /**
         * 从读写锁提取子锁。
         *
         * @param lock 读写锁
         * @return 读锁或写锁
         */
        abstract RLock transform(RReadWriteLock lock);
    }

}
