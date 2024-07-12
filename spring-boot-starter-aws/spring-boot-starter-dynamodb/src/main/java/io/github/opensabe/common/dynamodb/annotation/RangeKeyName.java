package io.github.opensabe.common.dynamodb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wangrushuang
 * @Description: 排序健，分区健和排序健可组成复合主见
 * @date 2019/11/720:28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RangeKeyName {
    String name() default "";
}
