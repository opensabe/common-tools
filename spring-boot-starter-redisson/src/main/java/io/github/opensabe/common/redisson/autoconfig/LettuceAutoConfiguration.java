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
package io.github.opensabe.common.redisson.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.context.annotation.Import;

import io.github.opensabe.common.redisson.config.LettuceConfiguration;

/**
 * Lettuce 客户端自动配置。
 * <p>
 * 在 Spring Data Redis 与多 Redis 自动配置之前注册 {@link LettuceConfiguration}，
 * 以便为 Lettuce {@link io.lettuce.core.resource.ClientResources} 注入 Micrometer 追踪与延迟采集。
 */
@AutoConfiguration(before = {DataRedisAutoConfiguration.class, MultiRedisAutoConfiguration.class})
@Import(LettuceConfiguration.class)
public class LettuceAutoConfiguration {
}
