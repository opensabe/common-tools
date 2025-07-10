package io.github.opensabe.common.redisson.annotation.slock;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @see org.redisson.api.RedissonClient#getFairLock(String)
 * @author heng.ma
 */
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SLock(name = "", lockFeature = SLock.LockFeature.FAIR)
public @interface FairLock {

    /**
     * 锁的名称表达式
     * @see Cacheable#cacheNames()
     */
    @AliasFor(annotation = SLock.class)
    String[] name();

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


    @AliasFor(annotation = SLock.class)
    SLock.LockType lockType() default SLock.LockType.BLOCK_LOCK;

}
