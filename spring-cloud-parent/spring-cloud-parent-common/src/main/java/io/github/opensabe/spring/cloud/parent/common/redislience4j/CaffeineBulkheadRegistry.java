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

import static java.util.Collections.emptyMap;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.core.registry.RegistryEventConsumer;

/**
 * @author maheng
 * @see io.github.resilience4j.bulkhead.internal.InMemoryBulkheadRegistry
 */
public class CaffeineBulkheadRegistry extends CaffeineResilienceRegistry<Bulkhead, BulkheadConfig> implements BulkheadRegistry {


    public CaffeineBulkheadRegistry(Map<String, BulkheadConfig> configs, RegistryEventConsumer<Bulkhead> registryEventConsumers, Map<String, String> tags) {
        super(configs, () -> BulkheadConfig.ofDefaults(), registryEventConsumers, tags);
    }

    @Override
    public Set<Bulkhead> getAllBulkheads() {
        return new HashSet<>(entryMap.values());
    }

    @Override
    public Bulkhead bulkhead(String name) {
        return bulkhead(name, emptyMap());
    }


    @Override
    public Bulkhead bulkhead(String name, Map<String, String> tags) {
        return bulkhead(name, getDefaultConfig(), getAllTags(tags));
    }


    @Override
    public Bulkhead bulkhead(String name, BulkheadConfig config) {
        return bulkhead(name, config, emptyMap());
    }


    @Override
    public Bulkhead bulkhead(String name, BulkheadConfig config, Map<String, String> tags) {
        return computeIfAbsent(name, () -> Bulkhead
                .of(name, Objects.requireNonNull(config, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }


    @Override
    public Bulkhead bulkhead(String name, Supplier<BulkheadConfig> bulkheadConfigSupplier) {
        return bulkhead(name, bulkheadConfigSupplier, emptyMap());
    }


    @Override
    public Bulkhead bulkhead(String name, Supplier<BulkheadConfig> bulkheadConfigSupplier, Map<String, String> tags) {
        return computeIfAbsent(name, () -> Bulkhead.of(name, Objects.requireNonNull(
                Objects.requireNonNull(bulkheadConfigSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
                CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }


    @Override
    public Bulkhead bulkhead(String name, String configName) {
        return bulkhead(name, configName, emptyMap());
    }


    @Override
    public Bulkhead bulkhead(String name, String configName, Map<String, String> tags) {
        return computeIfAbsent(name, () -> Bulkhead.of(name, getConfiguration(configName)
                .orElseThrow(() -> new ConfigurationNotFoundException(configName)), getAllTags(tags)));
    }
}
