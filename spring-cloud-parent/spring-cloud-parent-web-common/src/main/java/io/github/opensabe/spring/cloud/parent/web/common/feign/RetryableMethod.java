package io.github.opensabe.spring.cloud.parent.web.common.feign;

import java.lang.annotation.*;

/**
 * 标注这个 feign 方法或者 feign 类里面的所有方法都是可以重试
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RetryableMethod {
}
