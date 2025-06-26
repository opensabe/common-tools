package io.github.opensabe.common.redisson.annotation;

import io.github.opensabe.common.redisson.annotation.slock.*;
import io.github.opensabe.common.redisson.exceptions.RedissonLockException;
import org.redisson.api.LockOptions;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 在方法或者类上添加该注释后，自动添加基于 Redisson 的分布式锁
 * @deprecated since 2.0.0 use {@link io.github.opensabe.common.redisson.annotation.slock} instead
 * @see io.github.opensabe.common.redisson.annotation.slock.RedissonLock
 * @see ReadWriteLock
 * @see FairLock
 * @see SpinLock
 * @see FencedLock
 */
@Deprecated
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RedissonLock {

    /**
     * 一般通过 RedissonLockName 指定锁名称
     * 但如果锁和方法参数无关，则通过这个 name 指定
     * 如果 RedissonLockName 为空，这个 name 也是默认的 空字符串，则锁不生效
     */
    String name() default "";


    String prefix() default io.github.opensabe.common.redisson.annotation.slock.RedissonLock.DEFAULT_PREFIX;
    /**
     * 锁特性
     */
    LockFeature lockFeature() default LockFeature.DEFAULT;

    /**
     * 阻塞锁
     */
    int BLOCK_LOCK = 1;
    /**
     * try lock，未获取则不等待，直接抛出 RedissionClientException
     */
    int TRY_LOCK_NOWAIT = 2;
    /**
     * try lock，包含等待
     */
    int TRY_LOCK = 3;

    /**
     * 锁类型
     */
    int lockType() default BLOCK_LOCK;

    /**
     * 锁等待时间
     */
    long waitTime() default 1000;

    /**
     * 锁最长持有时间
     */
    long leaseTime() default -1;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    BackOffType backOffType() default BackOffType.EXPONENTIAL;

    /**
     * 这个参数在 LockFeature = SPIN， BackOffType = CONSTANT 使用
     */
    long backOffDelay() default 64L;

    /**
     * 以下三个参数在 LockFeature = SPIN， BackOffType = EXPONENTIAL 使用
     */
    long backOffMaxDelay()  default 128;
    long backOffInitialDelay() default 1;
    int backOffMultiplier() default 2;

    /**
     * 这个参数在 LockFeature = READ_WRITE 使用
     */
    ReadOrWrite readOrWrite() default ReadOrWrite.READ;

    enum LockFeature {
        /**
         * @see org.redisson.api.RedissonClient#getLock(String)
         */
        DEFAULT {
            @Override
            public RLock getLock(String name, RedissonLock content, RedissonClient redissonClient) {
                return redissonClient.getLock(name);
            }
        },
        /**
         * @see org.redisson.api.RedissonClient#getFairLock(String)
         */
        FAIR {
            @Override
            public RLock getLock(String name, RedissonLock content, RedissonClient redissonClient) {
                return redissonClient.getFairLock(name);
            }
        },
        /**
         * @see org.redisson.api.RedissonClient#getSpinLock(String)
         * @see org.redisson.api.RedissonClient#getSpinLock(String, LockOptions.BackOff)
         */
        SPIN {
            @Override
            public RLock getLock(String name, RedissonLock content, RedissonClient redissonClient) {
                return redissonClient.getSpinLock(name, content.backOffType().backOff(content));
            }
        },
        /**
         * @see org.redisson.api.RedissonClient#getReadWriteLock(String)
         */
        READ_WRITE {
            @Override
            public RLock getLock(String name, RedissonLock content, RedissonClient redissonClient) {
                return content.readOrWrite().transform(redissonClient.getReadWriteLock(name));
            }
        },

        /**
         * @see RedissonClient#getFencedLock(String)
         */
        FENCED {
            @Override
            public RLock getLock(String name, RedissonLock content, RedissonClient redissonClient) {
                return redissonClient.getFencedLock(name);
            }
        }
        ;

        public abstract RLock getLock (String name, RedissonLock content, RedissonClient redissonClient);
    }

    enum BackOffType {
        /**
         * @see LockOptions.ConstantBackOff
         */
        CONSTANT {
            @Override
            LockOptions.BackOff backOff(RedissonLock content) {
                return new LockOptions.ConstantBackOff()
                        .delay(content.backOffDelay());
            }
        },
        /**
         * @see LockOptions.ExponentialBackOff
         */
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

        abstract LockOptions.BackOff backOff (RedissonLock content);
    }

    enum ReadOrWrite {
        READ {
            @Override
            RLock transform(RReadWriteLock lock) {
                return lock.readLock();
            }
        },
        WRITE {
            @Override
            RLock transform(RReadWriteLock lock) {
                return lock.writeLock();
            }
        },
        ;

        abstract RLock transform (RReadWriteLock lock);
    }

    enum LockType {

        BLOCK_LOCK(RedissonLock.BLOCK_LOCK) {
            @Override
            public boolean lock(RedissonLock content, RLock lock) {
                lock.lock(content.leaseTime(), content.timeUnit());
                return true;
            }
        },
        /**
         * try lock 不等待，直接返回
         */
        TRY_LOCK_NOWAIT(RedissonLock.TRY_LOCK_NOWAIT) {
            @Override
            public boolean lock(RedissonLock content, RLock lock) {
                return lock.tryLock();
            }
        },
        /**
         * try lock，包含等待
         */
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

        LockType(int value) {
            this.value = value;
        }

        private final static Map<Integer, LockType> map = new ConcurrentHashMap<>(3);

        public abstract boolean lock (RedissonLock content, RLock lock);

        private final int value;


        public static LockType lockType (int value) {
            return map.computeIfAbsent(value, k -> Arrays.stream(values()).filter(e -> Objects.equals(e.value, value))
                    .findFirst()
                    .orElseThrow());
        }
    }
}
