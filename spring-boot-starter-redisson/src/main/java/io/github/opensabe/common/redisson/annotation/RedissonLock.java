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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.redisson.api.LockOptions;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import io.github.opensabe.common.redisson.annotation.slock.FairLock;
import io.github.opensabe.common.redisson.annotation.slock.FencedLock;
import io.github.opensabe.common.redisson.annotation.slock.ReadWriteLock;
import io.github.opensabe.common.redisson.annotation.slock.SpinLock;
import io.github.opensabe.common.redisson.exceptions.RedissonLockException;

/**
 * 在方法或类上声明 Redisson 分布式锁（已废弃，请使用 {@code slock} 包）。
 * <p>
 * 由 {@link io.github.opensabe.common.redisson.aop.lock.RedissonLockInterceptor} 拦截并加锁。
 *
 * @see io.github.opensabe.common.redisson.annotation.slock.RedissonLock
 * @see ReadWriteLock
 * @see FairLock
 * @see SpinLock
 * @see FencedLock
 * @deprecated since 2.0.0，请改用 {@link io.github.opensabe.common.redisson.annotation.slock} 包内注解
 */
@Deprecated(since = "2.0.0")
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RedissonLock {

    /** 阻塞加锁，对应 {@link org.redisson.api.RLock#lock(long, TimeUnit)}。 */
    int BLOCK_LOCK = 1;
    /** tryLock 不等待；失败抛出 {@link io.github.opensabe.common.redisson.exceptions.RedissonLockException}。 */
    int TRY_LOCK_NOWAIT = 2;
    /** tryLock 带等待时间与 lease。 */
    int TRY_LOCK = 3;

    /**
     * 锁名称 SpEL；与 {@link RedissonLockName} 二选一。
     * 若两者均为空字符串，则锁不生效。
     */
    String name() default "";

    /** 锁键前缀。 */
    String prefix() default io.github.opensabe.common.redisson.annotation.slock.RedissonLock.DEFAULT_PREFIX;

    /** Redisson 锁实现特性（可重入 / 公平 / 自旋 / 读写 / 栅栏）。 */
    LockFeature lockFeature() default LockFeature.DEFAULT;

    /** 加锁策略，取 {@link #BLOCK_LOCK}、{@link #TRY_LOCK_NOWAIT} 或 {@link #TRY_LOCK}。 */
    int lockType() default BLOCK_LOCK;

    /** tryLock 最长等待时间。 */
    long waitTime() default 1000;

    /** 锁 lease 时间；-1 表示看门狗续期。 */
    long leaseTime() default -1;

    /** {@link #waitTime()} 与 {@link #leaseTime()} 的时间单位。 */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

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

    /** Redisson 锁实现类型，决定调用哪个 {@link RedissonClient} API。 */
    enum LockFeature {
        /**
         * @see org.redisson.api.RedissonClient#getLock(String)
         */
        /** 可重入锁。 @see org.redisson.api.RedissonClient#getLock(String) */
        DEFAULT {
            @Override
            public RLock getLock(String name, RedissonLock content, RedissonClient redissonClient) {
                return redissonClient.getLock(name);
            }
        },
        /** 公平锁。 @see org.redisson.api.RedissonClient#getFairLock(String) */
        FAIR {
            @Override
            public RLock getLock(String name, RedissonLock content, RedissonClient redissonClient) {
                return redissonClient.getFairLock(name);
            }
        },
        /** 自旋锁，可配置退避。 */
        SPIN {
            @Override
            public RLock getLock(String name, RedissonLock content, RedissonClient redissonClient) {
                return redissonClient.getSpinLock(name, content.backOffType().backOff(content));
            }
        },
        /** 读写锁（读或写子锁）。 */
        READ_WRITE {
            @Override
            public RLock getLock(String name, RedissonLock content, RedissonClient redissonClient) {
                return content.readOrWrite().transform(redissonClient.getReadWriteLock(name));
            }
        },

        /** 栅栏锁（fenced lock）。 @see RedissonClient#getFencedLock(String) */
        FENCED {
            @Override
            public RLock getLock(String name, RedissonLock content, RedissonClient redissonClient) {
                return redissonClient.getFencedLock(name);
            }
        };

        /**
         * 按锁特性从 {@link RedissonClient} 获取 {@link RLock} 实例。
         *
         * @param name 已解析的锁键
         * @param content 注解实例
         * @param redissonClient Redisson 客户端
         * @return 对应类型的锁对象
         */
        public abstract RLock getLock(String name, RedissonLock content, RedissonClient redissonClient);
    }

    /** 自旋锁退避算法。 */
    enum BackOffType {
        /** 固定间隔退避。 @see LockOptions.ConstantBackOff */
        CONSTANT {
            @Override
            LockOptions.BackOff backOff(RedissonLock content) {
                return new LockOptions.ConstantBackOff()
                        .delay(content.backOffDelay());
            }
        },
        /** 指数退避。 @see LockOptions.ExponentialBackOff */
        EXPONENTIAL {
            @Override
            LockOptions.BackOff backOff(RedissonLock content) {
                return new LockOptions.ExponentialBackOff()
                        .initialDelay(content.backOffInitialDelay())
                        .maxDelay(content.backOffMaxDelay())
                        .multiplier(content.backOffMultiplier());
            }
        },
        ;

        /**
         * 根据注解参数构建 Redisson 退避配置。
         *
         * @param content 注解实例
         * @return 退避策略
         */
        abstract LockOptions.BackOff backOff(RedissonLock content);
    }

    /** 读写锁子锁类型。 */
    enum ReadOrWrite {
        /** 读锁。 */
        READ {
            @Override
            RLock transform(RReadWriteLock lock) {
                return lock.readLock();
            }
        },
        /** 写锁。 */
        WRITE {
            @Override
            RLock transform(RReadWriteLock lock) {
                return lock.writeLock();
            }
        },
        ;

        /**
         * 从读写锁中提取读锁或写锁。
         *
         * @param lock 读写锁
         * @return 子锁
         */
        abstract RLock transform(RReadWriteLock lock);
    }

    /** 加锁行为枚举，由 {@link #lockType()} 整型值映射。 */
    enum LockType {

        /** 阻塞加锁。 */
        BLOCK_LOCK(RedissonLock.BLOCK_LOCK) {
            @Override
            public boolean lock(RedissonLock content, RLock lock) {
                lock.lock(content.leaseTime(), content.timeUnit());
                return true;
            }
        },
        /** tryLock 不等待。 */
        TRY_LOCK_NOWAIT(RedissonLock.TRY_LOCK_NOWAIT) {
            @Override
            public boolean lock(RedissonLock content, RLock lock) {
                return lock.tryLock();
            }
        },
        /** tryLock 带等待。 */
        TRY_LOCK(RedissonLock.TRY_LOCK) {
            @Override
            public boolean lock(RedissonLock content, RLock lock) {
                try {
                    return lock.tryLock(content.waitTime(), content.leaseTime(), content.timeUnit());
                } catch (InterruptedException e) {
                    throw new RedissonLockException("can not get redisson lock", e);
                }
            }
        };

        /** {@link #lockType()} 整型值 → 枚举缓存。 */
        private static final Map<Integer, LockType> MAP = new ConcurrentHashMap<>(3);
        /** 对应 {@link RedissonLock#lockType()} 的整型常量。 */
        private final int value;

        LockType(int value) {
            this.value = value;
        }

        /**
         * 按整型 lockType 解析枚举；未知值抛出异常。
         *
         * @param value {@link RedissonLock#lockType()} 值
         * @return 对应枚举常量
         */
        public static LockType lockType(int value) {
            return MAP.computeIfAbsent(value, k -> Arrays.stream(values()).filter(e -> Objects.equals(e.value, value))
                    .findFirst()
                    .orElseThrow());
        }

        /**
         * 执行加锁逻辑。
         *
         * @param content 注解实例
         * @param lock 已获取的 {@link RLock}
         * @return 是否成功加锁
         */
        public abstract boolean lock(RedissonLock content, RLock lock);
    }
}
