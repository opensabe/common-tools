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

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.core.registry.RegistryEventConsumer;

/**
 * @author maheng
 * @see io.github.resilience4j.circuitbreaker.internal.InMemoryCircuitBreakerRegistry
 */
public class CaffeineCircuitBreakerRegistry extends CaffeineResilienceRegistry<CircuitBreaker, CircuitBreakerConfig> implements CircuitBreakerRegistry {


    public CaffeineCircuitBreakerRegistry(Map<String, CircuitBreakerConfig> configs, RegistryEventConsumer<CircuitBreaker> registryEventConsumers, Map<String, String> tags) {
        super(configs, () -> CircuitBreakerConfig.ofDefaults(), registryEventConsumers, tags);
    }

    @Override
    public Set<CircuitBreaker> getAllCircuitBreakers() {
        return new HashSet<>(entryMap.values());
    }


    @Override
    public CircuitBreaker circuitBreaker(String name) {
        return circuitBreaker(name, getDefaultConfig());
    }

    @Override
    public CircuitBreaker circuitBreaker(String name, Map<String, String> tags) {
        return circuitBreaker(name, getDefaultConfig(), tags);
    }


    @Override
    public CircuitBreaker circuitBreaker(String name, CircuitBreakerConfig config) {
        return circuitBreaker(name, config, emptyMap());
    }

    @Override
    public CircuitBreaker circuitBreaker(String name, CircuitBreakerConfig config,
                                         Map<String, String> tags) {
        return computeIfAbsent(name, () -> CircuitBreaker
                .of(name, Objects.requireNonNull(config, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }


    @Override
    public CircuitBreaker circuitBreaker(String name, String configName) {
        return circuitBreaker(name, configName, emptyMap());
    }

    @Override
    public CircuitBreaker circuitBreaker(String name, String configName, Map<String, String> tags) {
        return computeIfAbsent(name, () -> CircuitBreaker.of(name, getConfiguration(configName)
                .orElseThrow(() -> new ConfigurationNotFoundException(configName)), getAllTags(tags)));
    }


    @Override
    public CircuitBreaker circuitBreaker(String name,
                                         Supplier<CircuitBreakerConfig> circuitBreakerConfigSupplier) {
        return circuitBreaker(name, circuitBreakerConfigSupplier, emptyMap());
    }

    @Override
    public CircuitBreaker circuitBreaker(String name,
                                         Supplier<CircuitBreakerConfig> circuitBreakerConfigSupplier, Map<String, String> tags) {
        return computeIfAbsent(name, () -> CircuitBreaker.of(name, Objects.requireNonNull(
                Objects.requireNonNull(circuitBreakerConfigSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
                CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }
}
