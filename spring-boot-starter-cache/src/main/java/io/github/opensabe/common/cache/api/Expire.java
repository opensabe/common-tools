package io.github.opensabe.common.cache.api;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Expire {

    /**
     * 过期时间，单位默认为秒
     * @return
     */
    long value() default 0;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

}
