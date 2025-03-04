package io.github.opensabe.common.redisson.annotation;

import io.github.opensabe.common.redisson.exceptions.RedissonLockException;
import org.redisson.api.LockOptions;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SLock {

    String[] name();

    String prefix() default RedissonLockName.DEFAULT_PREFIX;

    int order() default 0;

    /**
     * 锁等待时间
     */
    long waitTime() default 1000l;

    /**
     * 锁最长持有时间
     */
    long leaseTime() default -1;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;


    LockType lockType() default LockType.BLOCK_LOCK;

    LockFeature lockFeature() default LockFeature.DEFAULT;

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

    enum LockType {

        BLOCK_LOCK {
            @Override
            public boolean lock(SLock content, RLock lock) {
                lock.lock(content.leaseTime(), content.timeUnit());
                return true;
            }
        },
        /**
         * try lock，不等待，直接返回
         */
        TRY_LOCK_NOWAIT {
            @Override
            public boolean lock(SLock content, RLock lock) {
                return lock.tryLock();
            }
        },
        /**
         * try lock，包含等待
         */
        TRY_LOCK {
            @Override
            public boolean lock(SLock content, RLock lock) {
                try {
                    return lock.tryLock(content.waitTime(), content.leaseTime(), content.timeUnit());
                } catch (InterruptedException e) {
                    throw new RedissonLockException ("can not get redisson lock", e);
                }
            }
        };

        public abstract boolean lock (SLock content,RLock lock);
    }

    enum LockFeature {
        /**
         * @see org.redisson.api.RedissonClient#getLock(String)
         */
        DEFAULT {
            @Override
            public RLock getLock(String name, SLock content, RedissonClient redissonClient) {
                return redissonClient.getLock(name);
            }
        },
        /**
         * @see org.redisson.api.RedissonClient#getFairLock(String)
         */
        FAIR {
            @Override
            public RLock getLock(String name, SLock content, RedissonClient redissonClient) {
                return redissonClient.getFairLock(name);
            }
        },
        /**
         * @see org.redisson.api.RedissonClient#getSpinLock(String)
         * @see org.redisson.api.RedissonClient#getSpinLock(String, LockOptions.BackOff)
         */
        SPIN {
            @Override
            public RLock getLock(String name, SLock content, RedissonClient redissonClient) {
                return redissonClient.getSpinLock(name, content.backOffType().backOff(content));
            }
        },
        /**
         * @see org.redisson.api.RedissonClient#getReadWriteLock(String)
         */
        READ_WRITE {
            @Override
            public RLock getLock(String name, SLock content, RedissonClient redissonClient) {
                return content.readOrWrite().transform(redissonClient.getReadWriteLock(name));
            }
        },

        /**
         * @see RedissonClient#getFencedLock(String)
         */
        FENCED {
            @Override
            public RLock getLock(String name, SLock content, RedissonClient redissonClient) {
                return redissonClient.getFencedLock(name);
            }
        }
        ;

        public abstract RLock getLock (String name, SLock content, RedissonClient redissonClient);
    }



    enum BackOffType {
        /**
         * @see LockOptions.ConstantBackOff
         */
        CONSTANT {
            @Override
            LockOptions.BackOff backOff(SLock content) {
                return new LockOptions.ConstantBackOff()
                        .delay(content.backOffDelay());
            }
        },
        /**
         * @see LockOptions.ExponentialBackOff
         */
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

        abstract LockOptions.BackOff backOff (SLock content);
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

}
