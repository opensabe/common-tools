package io.github.opensabe.common.redisson.annotation.bucket;


import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonBucketEvict {

    /**
     * 锁的名称表达式，锁的名称表达式。如果为空，则为类名+方法名
     * @see org.springframework.cache.annotation.Cacheable#cacheNames()
     */

    String name() default "";

    String prefix() default RedissonBucket.DEFAULT_PREFIX;
}
