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
package io.github.opensabe.common.redisson.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Redisson AOP Advisor 顺序配置，绑定前缀 {@code spring.redis.redisson.aop}。
 */
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "spring.redis.redisson.aop")
public class RedissonAopOrderProperties {

    /** Advisor 顺序，默认 {@link Ordered#LOWEST_PRECEDENCE}。 */
    private int order = Ordered.LOWEST_PRECEDENCE;
}
