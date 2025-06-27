package io.github.opensabe.common.redisson.annotation.bucket;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonBucket {

    String DEFAULT_PREFIX = "redisson:bucket:";

    /**
     * 锁的名称表达式，如果为空。则为类名+方法名
     * @see org.springframework.cache.annotation.Cacheable#cacheNames()
     */

    String name() default "";

    String prefix() default DEFAULT_PREFIX;


    CacheOption   option() default CacheOption.SET;

    /**
     * 缓存时间,默认60分钟
     */
    int ttl() default 60;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;
}
