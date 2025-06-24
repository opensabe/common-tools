package io.github.opensabe.common.redisson.annotation;


import io.github.opensabe.common.redisson.annotation.slock.RedissonLock;

import java.lang.annotation.*;

/**
 * 分布式锁名称注解
 * @deprecated use {@link io.github.opensabe.common.redisson.annotation.slock.RedissonLock} instead
 */
@Deprecated(forRemoval = true)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface RedissonLockName {


    String prefix() default RedissonLock.DEFAULT_PREFIX;

    String expression() default "";
}
