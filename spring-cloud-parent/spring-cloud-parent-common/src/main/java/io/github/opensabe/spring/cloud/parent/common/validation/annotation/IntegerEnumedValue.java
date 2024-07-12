package io.github.opensabe.spring.cloud.parent.common.validation.annotation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(IntegerEnumedValue.List.class)
@Documented
@Constraint(validatedBy = { })
public @interface IntegerEnumedValue {

    String message() default "${field} allowed in {value}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    int[] value();


    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
    @Retention(RUNTIME)
    @Documented
    @interface List {

        IntegerEnumedValue[] value();
    }
}
