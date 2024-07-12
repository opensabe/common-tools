package io.github.opensabe.common.dynamodb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wangrushuang
 * @Description: 分区健
 * @date 2019/11/720:27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HashKeyName {
    String name() default "";
}
