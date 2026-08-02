/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.spring.cloud.parent.common.validation.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 整型字段必须在指定允许值集合内；{@code null} 由其他约束处理。
 * <p>
 * 校验实现见 {@link io.github.opensabe.spring.cloud.parent.common.validation.IntegerEnumedValidator}。
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(IntegerEnumedValue.List.class)
@Documented
@Constraint(validatedBy = {})
public @interface IntegerEnumedValue {

    /** 校验失败时的默认消息模板。 */
    String message() default "allowed in {value}";

    /** 校验分组。 */
    Class<?>[] groups() default {};

    /** 负载类型。 */
    Class<? extends Payload>[] payload() default {};

    /** 允许的整型值列表。 */
    int[] value();

    /** 同一元素上重复注解的容器。 */
        /** 重复的 {@link IntegerEnumedValue} 实例。 */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {

        IntegerEnumedValue[] value();
    }
}
