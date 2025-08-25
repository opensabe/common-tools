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

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import io.github.resilience4j.core.registry.AbstractRegistry;
import io.github.resilience4j.core.registry.RegistryEventConsumer;

/**
 * 将RegistryStory换成caffeine
 *
 * @param <E> 组件eg: Retry
 * @param <C> 组件配置 eg: RetryConfig
 * @see AbstractRegistry
 */
public abstract class CaffeineResilienceRegistry<E, C> extends AbstractRegistry<E, C> {

    public CaffeineResilienceRegistry(Map<String, C> configs, Supplier<C> defaultConfig, RegistryEventConsumer<E> registryEventConsumer, Map<String, String> tags) {
        this(configs, defaultConfig, List.of(registryEventConsumer), tags);
    }

    public CaffeineResilienceRegistry(Map<String, C> configs, Supplier<C> defaultConfig, List<RegistryEventConsumer<E>> registryEventConsumers, Map<String, String> tags) {
        super(configs.computeIfAbsent(DEFAULT_CONFIG, key -> defaultConfig.get()), registryEventConsumers, tags, new CaffeineRegistryStore<>());
        this.configurations.putAll(configs);
    }
}
