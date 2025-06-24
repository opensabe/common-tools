package io.github.opensabe.common.redisson.annotation;

import java.lang.annotation.*;

/**
 * @deprecated  use {@link RedissonSemaphore#name()} instead
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface RedissonSemaphoreName {


    String prefix() default RedissonSemaphore.DEFAULT_PREFIX;

    String expression() default "";
}
