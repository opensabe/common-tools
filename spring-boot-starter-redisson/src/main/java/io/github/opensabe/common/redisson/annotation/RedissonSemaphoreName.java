package io.github.opensabe.common.redisson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface RedissonSemaphoreName {
    String DEFAULT_PREFIX = "redisson:semaphore:";

    String prefix() default DEFAULT_PREFIX;

    String expression() default "";
}
