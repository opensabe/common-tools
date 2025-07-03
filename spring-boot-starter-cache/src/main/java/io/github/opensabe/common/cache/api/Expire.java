package io.github.opensabe.common.cache.api;

import org.springframework.boot.autoconfigure.cache.CacheType;

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

    /**
     * 目前只能选择redis或者caffeine
     * <p>
     *     如果@Cacheable没有选择cacheManager，会优先从自定义的CacheManager中判断是否包含cacheName，
     *     如果预定义的CacheManager没有预先创建cacheName，会根据cacheType选择cacheManager
     * </p>
     */
    CacheType cacheType() default CacheType.NONE;
}
