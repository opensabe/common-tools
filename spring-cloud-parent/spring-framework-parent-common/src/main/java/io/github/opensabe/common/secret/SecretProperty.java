package io.github.opensabe.common.secret;

import java.lang.annotation.*;

/**
 * 标记敏感属性，在属性上添加@SecretProperty注解,即可
 * @author hengma
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface SecretProperty {
}
