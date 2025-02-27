package io.github.opensabe.common.redisson.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@SLock(name = "", lockFeature = SLock.LockFeature.DEFAULT)
public @interface Lock {

    @AliasFor(annotation = SLock.class)
    String name ();

    /**
     * 多个lock时排序用
     */
    @AliasFor(annotation = SLock.class)
    int order() default 0;

    /**
     * 锁类型
     */
    @AliasFor(annotation = SLock.class)
    int lockType() default SLock.BLOCK_LOCK;

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
}
