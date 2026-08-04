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
package io.github.opensabe.spring.cloud.parent.common.redislience4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

import static java.util.Collections.emptyMap;

/**
 * 基于 Caffeine 存储的 {@link io.github.resilience4j.timelimiter.TimeLimiterRegistry} 实现。
 * <p>
 * 替代默认 InMemory 实现，支持条目过期与 AutoCloseable 资源回收。
 *
 * @see io.github.resilience4j.core.registry.AbstractRegistry
 */
public class CaffeineTimeLimiterRegistry extends CaffeineResilienceRegistry<TimeLimiter, TimeLimiterConfig> implements TimeLimiterRegistry {


    /**
     * 构造 Registry。
     *
     * @param configs 命名配置
     * @param registryEventConsumers 事件消费者
     * @param tags 全局标签
     */
    public CaffeineTimeLimiterRegistry(Map<String, TimeLimiterConfig> configs, RegistryEventConsumer<TimeLimiter> registryEventConsumers, Map<String, String> tags) {
        super(configs, () -> TimeLimiterConfig.ofDefaults(), registryEventConsumers, tags);
    }

    @Override
    public Set<TimeLimiter> getAllTimeLimiters() {
        return new HashSet<>(entryMap.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeLimiter timeLimiter(final String name) {
        return timeLimiter(name, getDefaultConfig(), emptyMap());
    }

    @Override
    public TimeLimiter timeLimiter(String name, Map<String, String> tags) {
        return timeLimiter(name, getDefaultConfig(), tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeLimiter timeLimiter(final String name, final TimeLimiterConfig config) {
        return timeLimiter(name, config, emptyMap());
    }

    @Override
    public TimeLimiter timeLimiter(String name,
                                   TimeLimiterConfig timeLimiterConfig, Map<String, String> tags) {
        return computeIfAbsent(name, () -> TimeLimiter.of(name,
                Objects.requireNonNull(timeLimiterConfig, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeLimiter timeLimiter(final String name,
                                   final Supplier<TimeLimiterConfig> timeLimiterConfigSupplier) {
        return timeLimiter(name, timeLimiterConfigSupplier, emptyMap());
    }

    @Override
    public TimeLimiter timeLimiter(String name,
                                   Supplier<TimeLimiterConfig> timeLimiterConfigSupplier, Map<String, String> tags) {
        return computeIfAbsent(name, () -> TimeLimiter.of(name, Objects.requireNonNull(
                Objects.requireNonNull(timeLimiterConfigSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
                CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeLimiter timeLimiter(String name, String configName) {
        return timeLimiter(name, configName, emptyMap());
    }

    @Override
    public TimeLimiter timeLimiter(String name, String configName, Map<String, String> tags) {
        TimeLimiterConfig config = getConfiguration(configName)
                .orElseThrow(() -> new ConfigurationNotFoundException(configName));
        return timeLimiter(name, config, tags);
    }
}
