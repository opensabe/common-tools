package io.github.opensabe.springdoc.responses;


import java.lang.annotation.*;

/**
 * @see BizResponse
 * @author heng.ma
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface BizResponses {

    BizResponse[] value();

}
