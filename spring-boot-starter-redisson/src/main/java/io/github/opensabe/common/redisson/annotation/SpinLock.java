package io.github.opensabe.common.redisson.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SLock(name = "", lockFeature = SLock.LockFeature.DEFAULT)
public @interface SpinLock {

    @AliasFor(annotation = SLock.class)
    String[] name();

    @AliasFor(annotation = SLock.class)
    String prefix() default RedissonLockName.DEFAULT_PREFIX;

    @AliasFor(annotation = SLock.class)
    int order() default 0;

    /**
     * 锁等待时间
     */
    @AliasFor(annotation = SLock.class)
    long waitTime() default 1000l;

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

    @AliasFor(annotation = SLock.class)
    SLock.LockType lockType() default SLock.LockType.BLOCK_LOCK;

    @AliasFor(annotation = SLock.class)
    SLock.BackOffType backOffType() default SLock.BackOffType.EXPONENTIAL;

    /**
     * 这个参数在 LockFeature = SPIN， BackOffType = CONSTANT 使用
     */
    @AliasFor(annotation = SLock.class)
    long backOffDelay() default 64L;

    /**
     * 以下三个参数在 LockFeature = SPIN， BackOffType = EXPONENTIAL 使用
     */
    @AliasFor(annotation = SLock.class)
    long backOffMaxDelay()  default 128;
    @AliasFor(annotation = SLock.class)
    long backOffInitialDelay() default 1;
    @AliasFor(annotation = SLock.class)
    int backOffMultiplier() default 2;


}
