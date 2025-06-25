package io.github.opensabe.common.redisson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated  use {@link RedissonRateLimiter#name()} instead
 */
@Deprecated(forRemoval = true)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface RedissonRateLimiterName {


    String prefix() default RedissonRateLimiter.DEFAULT_PREFIX;

    String expression() default "";
}
