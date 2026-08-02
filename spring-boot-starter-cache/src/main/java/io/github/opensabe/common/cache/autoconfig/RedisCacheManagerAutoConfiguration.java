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
package io.github.opensabe.common.cache.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import io.github.opensabe.common.cache.config.RedisConfiguration;

/**
 * Redis 缓存管理器自动配置入口。
 * <p>
 * 在 {@link DataRedisAutoConfiguration} 之后、且存在 {@link RedisConnectionFactory} 时，
 * 导入 {@link RedisConfiguration} 以注册动态 Redis {@link org.springframework.cache.CacheManager}。
 * </p>
 *
 * @author heng.ma
 */
@AutoConfiguration(after = DataRedisAutoConfiguration.class)
@Import(RedisConfiguration.class)
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisCacheManagerAutoConfiguration {
}
