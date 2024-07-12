package io.github.opensabe.common.redisson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(
        {ElementType.METHOD, ElementType.TYPE}
)
public @interface RedissonSemaphore {
    /**
     * 注意，使用的不是 Redisson 的 Semaphore，而是更严谨的 PermitExpirableSemaphore
     * 可以通过 RedissonSemaphoreName 指定限流器名称
     * 对于不通过参数指定名称的，可以使用这个方法指定
     * 如果 RedissonSemaphoreName 为空，这个 name 也是默认的 空字符串，则限流器不生效
     */
    String name() default "";

    /**
     * 限流器总量
     */
    int totalPermits() default 100;

    /**
     * 被限流之后的表现
     * 默认是阻塞等待
     */
    Type type() default Type.BLOCK;

    enum Type {
        /**
         * 获取不到就阻塞等待
         */
        BLOCK,
        /**
         * 获取不到就抛异常
         */
        TRY,
        ;
    }

    /**
     * 锁最长持有时间，为 -1 则是无限等待（这个是 Redisson API 的设计）
     */
    long leaseTime() default -1;

    /**
     * 仅对 Type.TRY 生效，等待时间
     * 如果为负数则不等待
     */
    long waitTime() default -1L;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
