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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;

import org.springframework.context.annotation.Conditional;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

/**
 * 基于 {@code spring.cloud.config.profile} 的条件装配注解，语义类似 {@link org.springframework.context.annotation.Profile}。
 * <p>
 * 由 {@link SpringCloudProfileCondition} 评估是否匹配。
 *
 * @see org.springframework.context.annotation.Profile
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(SpringCloudProfileCondition.class)
public @interface ConditionOnSpringCloudConfigProfile {
    /** 待匹配的 profile 表达式数组。 */
    String[] value();

    /** profile 匹配谓词，默认精确相等。 */
    Predicate predicate() default Predicate.equals;

    /**
     * {@code spring.cloud.config.profile} 与注解 {@link #value()} 的匹配策略。
     */
    enum Predicate {

        /** 精确相等（支持逗号分隔多值任一命中）。 */
        equals(Objects::equals),

        /** 正则匹配。 */
        regex((s, s2) -> s.matches(s2)),

        /** Ant 风格路径匹配（分隔符 {@code -}）。 */
        ant((s, s2) -> new AntPathMatcher("-").match(s2, s)),
        ;

        private BiFunction<String, String, Boolean> predicate;

        Predicate(BiFunction<String, String, Boolean> predicate) {
            this.predicate = predicate;
        }

        /**
         * 判断 {@code s}（实际 profile，可逗号分隔）是否与模式 {@code s2} 匹配。
         *
         * @param s  实际 profile 值
         * @param s2 注解中的模式
         * @return 是否匹配
         */
        public boolean matches(String s, String s2) {
            return Arrays.stream(StringUtils.commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(s)))
                    .anyMatch(s3 -> predicate.apply(s3, s2));
        }
    }
}
