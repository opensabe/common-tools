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
package io.github.opensabe.springdoc.responses;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.*;

import java.lang.annotation.*;

/**
 * 内部动态代理生成@ApiResponse注解时使用的无关变量
 * 禁止外部使用
 * @author heng.ma
 */
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@interface Useless {

    Header[] headers() default {};
    Link[] links() default {};



    Schema schema() default @Schema();
    Schema[] schemas() default {};

    ArraySchema array() default @ArraySchema();
    Encoding[] encoding() default {};
    Extension[] extensions() default {};
    ExampleObject[] examples() default {};
    DependentSchema[] dependentSchemas() default {};
    DependentRequired[] dependentRequiredMap() default {};
    StringToClassMapItem[] patternProperties() default {};


    ExternalDocumentation externalDocs() default @ExternalDocumentation();
    DiscriminatorMapping[] discriminatorMapping() default {};
}
