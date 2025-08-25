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

import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

import java.util.*;
import java.util.function.Supplier;

/**
 * @see io.github.resilience4j.retry.internal.InMemoryRetryRegistry
 * @author maheng
 */
public class CaffeineRetryRegistry extends CaffeineResilienceRegistry<Retry, RetryConfig> implements RetryRegistry {


    public CaffeineRetryRegistry(Map<String, RetryConfig> configs, RegistryEventConsumer<Retry> registryEventConsumer, Map<String, String> tags) {
        super(configs, () -> RetryConfig.ofDefaults(), registryEventConsumer, tags);
    }

    @Override
    public Set<Retry> getAllRetries() {
        return new HashSet<>(entryMap.values());
    }

    @Override
    public Retry retry(String name) {
        return retry(name, getDefaultConfig());
    }

    @Override
    public Retry retry(String name, Map<String, String> tags) {
        return retry(name, getDefaultConfig(), tags);
    }

    @Override
    public Retry retry(String name, RetryConfig config) {
        return retry(name, config, Collections.emptyMap());
    }

    @Override
    public Retry retry(String name, RetryConfig config, Map<String, String> tags) {
        return computeIfAbsent(name, () -> Retry
                .of(name, Objects.requireNonNull(config, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public Retry retry(String name, Supplier<RetryConfig> retryConfigSupplier) {
        return retry(name, retryConfigSupplier, Collections.emptyMap());
    }

    @Override
    public Retry retry(String name, Supplier<RetryConfig> retryConfigSupplier, Map<String, String> tags) {
        return computeIfAbsent(name, () -> Retry.of(name, Objects.requireNonNull(
                Objects.requireNonNull(retryConfigSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
                CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public Retry retry(String name, String configName) {
        return retry(name, configName, Collections.emptyMap());
    }

    @Override
    public Retry retry(String name, String configName, Map<String, String> tags) {
        return computeIfAbsent(name, () -> Retry.of(name, getConfiguration(configName)
                .orElseThrow(() -> new ConfigurationNotFoundException(configName)), getAllTags(tags)));
    }
}
