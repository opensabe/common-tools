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
package io.github.opensabe.spring.cloud.parent.common.condition;

import org.springframework.context.annotation.Conditional;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @see org.springframework.context.annotation.Profile
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(SpringCloudProfileCondition.class)
public @interface ConditionOnSpringCloudConfigProfile {
    String[] value();

    Predicate predicate() default Predicate.equals;

    enum Predicate {

        equals(Objects::equals),

        regex((s, s2) -> s.matches(s2)),

        ant((s, s2) -> new AntPathMatcher("-").match(s2,s)),
        ;

        Predicate(BiFunction<String, String, Boolean> predicate) {
            this.predicate = predicate;
        }

        private BiFunction<String,String,Boolean> predicate;

        public boolean matches (String s, String s2) {
            return Arrays.stream(StringUtils.commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(s)))
                    .anyMatch(s3 -> predicate.apply(s3,s2));
        }
    }
}
