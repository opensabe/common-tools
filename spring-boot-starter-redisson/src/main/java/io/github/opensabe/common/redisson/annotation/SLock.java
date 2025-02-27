package io.github.opensabe.common.redisson.annotation;

import org.redisson.api.LockOptions;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 单个lock
 * 包含所有Lock的所有lock方式
 * @see LockFeature
 * @author hengma
 */
@Repeatable(MLock.class)
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SLock {
    /**
     * key表达式. 跟 Cacheable保持一致
     */
    String name();

    /**
     * 多个lock时排序用
     */
    int order() default 0;

    /**
     * 锁特性
     */
    LockFeature lockFeature() default LockFeature.DEFAULT;

    /**
     * 阻塞锁
     */
    int BLOCK_LOCK = 1;
    /**
     * try lock，未获取则不等待，直接抛出 RedissonLockException
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
    long waitTime() default 1000l;

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
    RedissonLock.ReadOrWrite readOrWrite() default RedissonLock.ReadOrWrite.READ;

    enum LockFeature {
        /**
         * @see org.redisson.api.RedissonClient#getLock(String)
         */
        DEFAULT,
        /**
         * @see org.redisson.api.RedissonClient#getFairLock(String)
         */
        FAIR,
        /**
         * @see org.redisson.api.RedissonClient#getSpinLock(String)
         * @see org.redisson.api.RedissonClient#getSpinLock(String, LockOptions.BackOff)
         */
        SPIN,
        /**
         * @see org.redisson.api.RedissonClient#getReadWriteLock(String)
         */
        READ_WRITE,
        ;
    }

    enum BackOffType {
        /**
         * @see LockOptions.ConstantBackOff
         */
        CONSTANT {
            @Override
            public LockOptions.BackOff lockOptions(SLock sLock) {
                return new LockOptions.ConstantBackOff()
                        .delay(sLock.backOffDelay());
            }
        },
        /**
         * @see LockOptions.ExponentialBackOff
         */
        EXPONENTIAL {
            @Override
            public LockOptions.BackOff lockOptions(SLock sLock) {
                return new LockOptions.ExponentialBackOff()
                        .initialDelay(sLock.backOffInitialDelay())
                        .maxDelay(sLock.backOffMaxDelay())
                        .multiplier(sLock.backOffMultiplier());
            }
        },
        ;

        public abstract LockOptions.BackOff lockOptions (SLock sLock);
    }

    enum ReadOrWrite {
        READ(RReadWriteLock::readLock),
        WRITE(RReadWriteLock::writeLock),
        ;

        private final Function<RReadWriteLock, RLock> transform;

        ReadOrWrite(Function<RReadWriteLock, RLock> transform) {
            this.transform = transform;
        }
    }
}
