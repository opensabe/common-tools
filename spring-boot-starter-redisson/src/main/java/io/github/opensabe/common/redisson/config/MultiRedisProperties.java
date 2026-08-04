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

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 多 Redis 连接配置属性，绑定前缀 {@code spring.data.redis}。
 * <p>
 * 启用 {@link #enableMulti} 后，{@link #multi} 中须包含 {@link #DEFAULT} 条目作为默认连接；
 * {@link RedissonClient} 仅消费 default 条目，Lettuce 路由由 {@code MultiRedisLettuceConnectionFactory} 负责。
 */
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "spring.data.redis")
public class MultiRedisProperties {

    /** 默认连接在 {@link #multi} 中的配置键名。 */
    public static final String DEFAULT = "default";

    /** 是否启用多 Redis 模式；对应 {@code spring.data.redis.enable-multi}。 */
    private boolean enableMulti = false;

    /** 命名 Redis 连接配置，键为逻辑名称，值为标准 {@link DataRedisProperties}。 */
    private Map<String, DataRedisProperties> multi;
}
