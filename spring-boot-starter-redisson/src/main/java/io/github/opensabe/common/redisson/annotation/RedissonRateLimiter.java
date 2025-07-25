package io.github.opensabe.common.redisson.annotation;

import org.redisson.api.RateType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(
        {ElementType.METHOD, ElementType.TYPE}
)
public @interface RedissonRateLimiter {
    String DEFAULT_PREFIX = "redisson:rateLimiter:";
    /**
     * 可以通过 RedissonRateLimiterName 指定限流器名称
     * 对于不通过参数指定名称的，可以使用这个方法指定
     * 如果 RedissonRateLimiterName 为空，这个 name 也是默认的 空字符串，则限流器不生效
     */
    String name() default "";


    String prefix() default DEFAULT_PREFIX;

    /**
     * 每次限流器获取的量
     */
    long permits() default 1;

    /**
     * 被限流之后的表现
     * 默认是阻塞等待
     */
    Type type() default Type.BLOCK;

    /**
     * @see RateType
     */
    RateType rateType();

    /**
     * 时间区间
     */
    long rateInterval();

    /**
     * 在 rateInterval 内，最多有多少个 permits
     */
    long rate();

    /**
     * 时间区间单位
     */
    TimeUnit rateIntervalUnit();

    /**
     * 令牌存活时间,keepAliveTimeUnit()
     */
    long keepAlive() default 0L;
    /**
     * 时间单位
     */
    TimeUnit keepAliveTimeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 仅对 Type.TRY 生效，等待时间
     * 如果为负数则不等待
     */
    long waitTime() default -1L;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    enum Type {
        /**
         * 获取不到就阻塞等待
         */
        BLOCK,
        /**
         * 获取不到就抛异常
         */
        TRY
    }
}
