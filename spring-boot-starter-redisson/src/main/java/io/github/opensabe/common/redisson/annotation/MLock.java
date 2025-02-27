package io.github.opensabe.common.redisson.annotation;

import java.lang.annotation.*;

/**
 * 同时获取多把锁
 * @author hengma
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MLock {

    SLock[] value();
}
