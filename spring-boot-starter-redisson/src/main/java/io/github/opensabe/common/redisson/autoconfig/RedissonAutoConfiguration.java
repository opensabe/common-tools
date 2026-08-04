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

import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.context.annotation.Import;

import io.github.opensabe.common.redisson.config.RedissonAnnotationConfiguration;
import io.github.opensabe.common.redisson.config.RedissonAopOrderProperties;
import io.github.opensabe.common.redisson.config.RedissonClientBeanPostProcessor;
import io.github.opensabe.common.redisson.config.RedissonScheduleProperties;

/**
 * Redisson 自动配置入口。
 * <p>
 * 必须手动调整顺序：{@link DataRedisAutoConfiguration} 须在 {@link RedissonAutoConfigurationV2} 之前执行，
 * 否则后者会覆盖前者，{@link org.springframework.data.redis.connection.RedisConnectionFactory}
 * 将被替换为 {@code RedissonConnectionFactory}。
 */
@EnableConfigurationProperties({RedissonScheduleProperties.class, RedissonAopOrderProperties.class})
@AutoConfiguration(before = RedissonAutoConfigurationV2.class)
@Import({RedissonClientBeanPostProcessor.class, RedissonAnnotationConfiguration.class, DataRedisAutoConfiguration.class})
public class RedissonAutoConfiguration {
}
