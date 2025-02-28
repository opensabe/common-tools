package io.github.opensabe.common.redisson.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Repeatable(ReadWriteLock.Locks.class)
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SLock(name = "", lockFeature = SLock.LockFeature.DEFAULT)
public @interface ReadWriteLock {

    @AliasFor(annotation = SLock.class)
    String name();

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
    SLock.ReadOrWrite readOrWrite() default SLock.ReadOrWrite.READ;

    @Documented
    @Inherited
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @SLock.Locks({})
    @interface Locks {

        @AliasFor(annotation = SLock.Locks.class)
        ReadWriteLock[] value();
    }
}
