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
package io.github.opensabe.spring.cloud.parent.common.config;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static java.util.Optional.ofNullable;

import io.github.opensabe.spring.cloud.parent.common.redislience4j.CaffeineBulkheadRegistry;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CaffeineCircuitBreakerRegistry;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CaffeineRateLimiterRegistry;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CaffeineRetryRegistry;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CaffeineThreadPoolBulkheadRegistry;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CaffeineTimeLimiterRegistry;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.bulkhead.event.BulkheadEvent;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import io.github.resilience4j.common.CompositeCustomizer;
import io.github.resilience4j.common.bulkhead.configuration.BulkheadConfigCustomizer;
import io.github.resilience4j.common.bulkhead.configuration.CommonBulkheadConfigurationProperties;
import io.github.resilience4j.common.bulkhead.configuration.CommonThreadPoolBulkheadConfigurationProperties;
import io.github.resilience4j.common.bulkhead.configuration.ThreadPoolBulkheadConfigCustomizer;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.github.resilience4j.common.circuitbreaker.configuration.CommonCircuitBreakerConfigurationProperties;
import io.github.resilience4j.common.ratelimiter.configuration.CommonRateLimiterConfigurationProperties;
import io.github.resilience4j.common.ratelimiter.configuration.RateLimiterConfigCustomizer;
import io.github.resilience4j.common.retry.configuration.CommonRetryConfigurationProperties;
import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import io.github.resilience4j.common.timelimiter.configuration.CommonTimeLimiterConfigurationProperties;
import io.github.resilience4j.common.timelimiter.configuration.TimeLimiterConfigCustomizer;
import io.github.resilience4j.consumer.EventConsumerRegistry;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.event.RateLimiterEvent;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.event.RetryEvent;
import io.github.resilience4j.spring6.bulkhead.configure.BulkheadConfigurationProperties;
import io.github.resilience4j.spring6.circuitbreaker.configure.CircuitBreakerConfigurationProperties;
import io.github.resilience4j.spring6.ratelimiter.configure.RateLimiterConfigurationProperties;
import io.github.resilience4j.spring6.retry.configure.RetryConfigurationProperties;
import io.github.resilience4j.spring6.timelimiter.configure.TimeLimiterConfigurationProperties;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.event.TimeLimiterEvent;

/**
 * 自己创建resilience4j相关组件的registry，代替InMemoryXXX
 *
 * @author maheng
 */
public class Resilience4jConfiguration {

    /**
     * Retry
     *
     * @author maheng
     */
    @Configuration(proxyBeanMethods = false)
    public static class RetryConfiguration {

        @Bean
        @Primary
        public RetryRegistry retryRegistry(RetryConfigurationProperties retryConfigurationProperties,
                                           EventConsumerRegistry<RetryEvent> retryEventConsumerRegistry,
                                           RegistryEventConsumer<Retry> retryRegistryEventConsumer,
                                           @Qualifier("compositeRetryCustomizer") CompositeCustomizer<RetryConfigCustomizer> compositeRetryCustomizer) {
            RetryRegistry retryRegistry = createRetryRegistry(retryConfigurationProperties,
                    retryRegistryEventConsumer, compositeRetryCustomizer);
            registerEventConsumer(retryRegistry, retryEventConsumerRegistry,
                    retryConfigurationProperties);
            retryConfigurationProperties.getInstances()
                    .forEach((name, properties) ->
                            retryRegistry.retry(name, retryConfigurationProperties
                                    .createRetryConfig(name, compositeRetryCustomizer)));
            return retryRegistry;
        }

        private void registerEventConsumer(RetryRegistry retryRegistry,
                                           EventConsumerRegistry<RetryEvent> eventConsumerRegistry,
                                           RetryConfigurationProperties properties) {
            retryRegistry.getEventPublisher()
                    .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry(), properties))
                    .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry(), properties));
        }

        private void registerEventConsumer(EventConsumerRegistry<RetryEvent> eventConsumerRegistry,
                                           Retry retry, RetryConfigurationProperties retryConfigurationProperties) {
            int eventConsumerBufferSize = Optional
                    .ofNullable(retryConfigurationProperties.getBackendProperties(retry.getName()))
                    .map(CommonRetryConfigurationProperties.InstanceProperties::getEventConsumerBufferSize)
                    .orElse(100);
            retry.getEventPublisher().onEvent(
                    eventConsumerRegistry.createEventConsumer(retry.getName(), eventConsumerBufferSize));
        }

        private RetryRegistry createRetryRegistry(
                RetryConfigurationProperties retryConfigurationProperties,
                RegistryEventConsumer<Retry> retryRegistryEventConsumer,
                CompositeCustomizer<RetryConfigCustomizer> compositeRetryCustomizer) {
            Map<String, RetryConfig> configs = retryConfigurationProperties.getConfigs()
                    .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> retryConfigurationProperties
                                    .createRetryConfig(entry.getValue(), compositeRetryCustomizer,
                                            entry.getKey())));

            return new CaffeineRetryRegistry(configs, retryRegistryEventConsumer, Map.copyOf(retryConfigurationProperties.getTags()));
        }
    }


    /**
     * 断路器
     *
     * @author maheng
     */
    @Configuration(proxyBeanMethods = false)
    public static class CircuitBreakerConfiguration {

        @Bean
        @Primary
        public CircuitBreakerRegistry circuitBreakerRegistry(
                CircuitBreakerConfigurationProperties circuitBreakerProperties,
                EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry,
                RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer,
                @Qualifier("compositeCircuitBreakerCustomizer") CompositeCustomizer<CircuitBreakerConfigCustomizer> compositeCircuitBreakerCustomizer) {

            CircuitBreakerRegistry circuitBreakerRegistry = createCircuitBreakerRegistry(
                    circuitBreakerProperties, circuitBreakerRegistryEventConsumer,
                    compositeCircuitBreakerCustomizer);
            registerEventConsumer(circuitBreakerProperties, circuitBreakerRegistry, eventConsumerRegistry);
            // then pass the map here
            initCircuitBreakerRegistry(circuitBreakerProperties, circuitBreakerRegistry, compositeCircuitBreakerCustomizer);
            return circuitBreakerRegistry;
        }

        private void registerEventConsumer(CircuitBreakerConfigurationProperties circuitBreakerProperties,
                                           CircuitBreakerRegistry circuitBreakerRegistry,
                                           EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry) {
            circuitBreakerRegistry.getEventPublisher()
                    .onEntryAdded(event -> registerEventConsumer(circuitBreakerProperties, eventConsumerRegistry, event.getAddedEntry()))
                    .onEntryReplaced(event -> registerEventConsumer(circuitBreakerProperties, eventConsumerRegistry, event.getNewEntry()));
        }

        private void registerEventConsumer(
                CircuitBreakerConfigurationProperties circuitBreakerProperties,
                EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry,
                CircuitBreaker circuitBreaker) {
            int eventConsumerBufferSize = circuitBreakerProperties
                    .findCircuitBreakerProperties(circuitBreaker.getName())
                    .map(CommonCircuitBreakerConfigurationProperties.InstanceProperties::getEventConsumerBufferSize)
                    .orElse(100);
            circuitBreaker.getEventPublisher().onEvent(eventConsumerRegistry
                    .createEventConsumer(circuitBreaker.getName(), eventConsumerBufferSize));
        }

        private CircuitBreakerRegistry createCircuitBreakerRegistry(
                CircuitBreakerConfigurationProperties circuitBreakerProperties,
                RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer,
                CompositeCustomizer<CircuitBreakerConfigCustomizer> customizerMap) {

            Map<String, CircuitBreakerConfig> configs = circuitBreakerProperties.getConfigs()
                    .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> circuitBreakerProperties
                                    .createCircuitBreakerConfig(entry.getKey(), entry.getValue(),
                                            customizerMap)));

            return new CaffeineCircuitBreakerRegistry(configs, circuitBreakerRegistryEventConsumer, Map.copyOf(circuitBreakerProperties.getTags()));
        }

        private void initCircuitBreakerRegistry(
                CircuitBreakerConfigurationProperties circuitBreakerProperties,
                CircuitBreakerRegistry circuitBreakerRegistry,
                CompositeCustomizer<CircuitBreakerConfigCustomizer> customizerMap) {
            circuitBreakerProperties.getInstances().forEach(
                    (name, properties) -> circuitBreakerRegistry.circuitBreaker(name,
                            circuitBreakerProperties
                                    .createCircuitBreakerConfig(name, properties, customizerMap))
            );
        }
    }


    /**
     * 实例隔离
     *
     * @author maheng
     */
    @Configuration(proxyBeanMethods = false)
    public static class BulkheadConfiguration {
        @Bean
        @Primary
        public BulkheadRegistry bulkheadRegistry(
                BulkheadConfigurationProperties bulkheadConfigurationProperties,
                EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry,
                RegistryEventConsumer<Bulkhead> bulkheadRegistryEventConsumer,
                @Qualifier("compositeBulkheadCustomizer") CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer) {
            BulkheadRegistry bulkheadRegistry = createBulkheadRegistry(bulkheadConfigurationProperties,
                    bulkheadRegistryEventConsumer, compositeBulkheadCustomizer);
            registerEventConsumer(bulkheadRegistry, bulkheadEventConsumerRegistry,
                    bulkheadConfigurationProperties);
            bulkheadConfigurationProperties.getInstances().forEach((name, properties) ->
                    bulkheadRegistry
                            .bulkhead(name, bulkheadConfigurationProperties
                                    .createBulkheadConfig(properties, compositeBulkheadCustomizer,
                                            name)));
            return bulkheadRegistry;
        }

        private BulkheadRegistry createBulkheadRegistry(
                BulkheadConfigurationProperties bulkheadConfigurationProperties,
                RegistryEventConsumer<Bulkhead> bulkheadRegistryEventConsumer,
                CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer) {
            Map<String, BulkheadConfig> configs = bulkheadConfigurationProperties.getConfigs()
                    .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> bulkheadConfigurationProperties.createBulkheadConfig(entry.getValue(),
                                    compositeBulkheadCustomizer, entry.getKey())));
            return new CaffeineBulkheadRegistry(configs, bulkheadRegistryEventConsumer, Map.copyOf(bulkheadConfigurationProperties.getTags()));
        }

        private void registerEventConsumer(BulkheadRegistry bulkheadRegistry,
                                           EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry,
                                           BulkheadConfigurationProperties properties) {
            bulkheadRegistry.getEventPublisher()
                    .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry(), properties))
                    .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry(), properties));
        }

        private void registerEventConsumer(EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry,
                                           Bulkhead bulkHead, BulkheadConfigurationProperties bulkheadConfigurationProperties) {
            int eventConsumerBufferSize = Optional
                    .ofNullable(bulkheadConfigurationProperties.getBackendProperties(bulkHead.getName()))
                    .map(CommonBulkheadConfigurationProperties.InstanceProperties::getEventConsumerBufferSize)
                    .orElse(100);
            bulkHead.getEventPublisher().onEvent(
                    eventConsumerRegistry.createEventConsumer(bulkHead.getName(), eventConsumerBufferSize));
        }
    }

    /**
     * 线程隔离
     *
     * @author maheng
     */
    @Configuration(proxyBeanMethods = false)
    public static class ThreadPoolBulkheadConfiguration {
        @Bean
        @Primary
        public ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry(
                CommonThreadPoolBulkheadConfigurationProperties bulkheadConfigurationProperties,
                EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry,
                RegistryEventConsumer<ThreadPoolBulkhead> threadPoolBulkheadRegistryEventConsumer,
                @Qualifier("compositeThreadPoolBulkheadCustomizer") CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> compositeThreadPoolBulkheadCustomizer) {
            ThreadPoolBulkheadRegistry bulkheadRegistry = createBulkheadRegistry(
                    bulkheadConfigurationProperties, threadPoolBulkheadRegistryEventConsumer,
                    compositeThreadPoolBulkheadCustomizer);
            registerEventConsumer(bulkheadRegistry, bulkheadEventConsumerRegistry,
                    bulkheadConfigurationProperties);
            bulkheadConfigurationProperties.getBackends().forEach((name, properties) -> bulkheadRegistry
                    .bulkhead(name, bulkheadConfigurationProperties
                            .createThreadPoolBulkheadConfig(name, compositeThreadPoolBulkheadCustomizer)));
            return bulkheadRegistry;
        }

        private ThreadPoolBulkheadRegistry createBulkheadRegistry(
                CommonThreadPoolBulkheadConfigurationProperties threadPoolBulkheadConfigurationProperties,
                RegistryEventConsumer<ThreadPoolBulkhead> threadPoolBulkheadRegistryEventConsumer,
                CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> compositeThreadPoolBulkheadCustomizer) {
            Map<String, ThreadPoolBulkheadConfig> configs = threadPoolBulkheadConfigurationProperties
                    .getConfigs()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> threadPoolBulkheadConfigurationProperties
                                    .createThreadPoolBulkheadConfig(entry.getValue(),
                                            compositeThreadPoolBulkheadCustomizer, entry.getKey())));
            return new CaffeineThreadPoolBulkheadRegistry(configs, threadPoolBulkheadRegistryEventConsumer, Map.copyOf(threadPoolBulkheadConfigurationProperties.getTags()));
        }

        /**
         * Registers the post creation consumer function that registers the consumer events to the
         * bulkheads.
         *
         * @param bulkheadRegistry      The BulkHead registry.
         * @param eventConsumerRegistry The event consumer registry.
         */
        private void registerEventConsumer(ThreadPoolBulkheadRegistry bulkheadRegistry,
                                           EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry,
                                           CommonThreadPoolBulkheadConfigurationProperties properties) {
            bulkheadRegistry.getEventPublisher()
                    .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry(), properties))
                    .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry(), properties));
        }

        private void registerEventConsumer(EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry,
                                           ThreadPoolBulkhead bulkHead,
                                           CommonThreadPoolBulkheadConfigurationProperties bulkheadConfigurationProperties) {
            int eventConsumerBufferSize = ofNullable(bulkheadConfigurationProperties.getBackendProperties(bulkHead.getName()))
                    .map(CommonThreadPoolBulkheadConfigurationProperties.InstanceProperties::getEventConsumerBufferSize)
                    .orElse(100);
            bulkHead.getEventPublisher().onEvent(eventConsumerRegistry.createEventConsumer(
                    String.join("-", ThreadPoolBulkhead.class.getSimpleName(), bulkHead.getName()),
                    eventConsumerBufferSize));
        }
    }

    /**
     * 限流器，不常用，一般使用redisson限流器
     *
     * @author maheng
     */
    @Configuration(proxyBeanMethods = false)
    public static class RateLimiterConfiguration {
        @Bean
        public RateLimiterRegistry rateLimiterRegistry(
                RateLimiterConfigurationProperties rateLimiterProperties,
                EventConsumerRegistry<RateLimiterEvent> rateLimiterEventsConsumerRegistry,
                RegistryEventConsumer<RateLimiter> rateLimiterRegistryEventConsumer,
                @Qualifier("compositeRateLimiterCustomizer") CompositeCustomizer<RateLimiterConfigCustomizer> compositeRateLimiterCustomizer) {
            RateLimiterRegistry rateLimiterRegistry = createRateLimiterRegistry(rateLimiterProperties,
                    rateLimiterRegistryEventConsumer, compositeRateLimiterCustomizer);
            registerEventConsumer(rateLimiterRegistry, rateLimiterEventsConsumerRegistry,
                    rateLimiterProperties);
            rateLimiterProperties.getInstances().forEach(
                    (name, properties) ->
                            rateLimiterRegistry
                                    .rateLimiter(name, rateLimiterProperties
                                            .createRateLimiterConfig(properties, compositeRateLimiterCustomizer, name))
            );
            return rateLimiterRegistry;
        }

        private RateLimiterRegistry createRateLimiterRegistry(
                RateLimiterConfigurationProperties rateLimiterConfigurationProperties,
                RegistryEventConsumer<RateLimiter> rateLimiterRegistryEventConsumer,
                CompositeCustomizer<RateLimiterConfigCustomizer> compositeRateLimiterCustomizer) {
            Map<String, RateLimiterConfig> configs = rateLimiterConfigurationProperties.getConfigs()
                    .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> rateLimiterConfigurationProperties
                                    .createRateLimiterConfig(entry.getValue(), compositeRateLimiterCustomizer,
                                            entry.getKey())));

            return new CaffeineRateLimiterRegistry(configs, rateLimiterRegistryEventConsumer,
                    Map.copyOf(rateLimiterConfigurationProperties.getTags()));
        }

        private void registerEventConsumer(RateLimiterRegistry rateLimiterRegistry,
                                           EventConsumerRegistry<RateLimiterEvent> eventConsumerRegistry,
                                           RateLimiterConfigurationProperties properties) {
            rateLimiterRegistry.getEventPublisher()
                    .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry(), properties))
                    .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry(), properties));
        }

        private void registerEventConsumer(
                EventConsumerRegistry<RateLimiterEvent> eventConsumerRegistry, RateLimiter rateLimiter,
                RateLimiterConfigurationProperties rateLimiterConfigurationProperties) {
            CommonRateLimiterConfigurationProperties.InstanceProperties limiterProperties = rateLimiterConfigurationProperties.getInstances()
                    .get(rateLimiter.getName());
            if (limiterProperties != null && limiterProperties.getSubscribeForEvents() != null
                    && limiterProperties.getSubscribeForEvents()) {
                rateLimiter.getEventPublisher().onEvent(
                        eventConsumerRegistry.createEventConsumer(rateLimiter.getName(),
                                limiterProperties.getEventConsumerBufferSize() != null
                                        && limiterProperties.getEventConsumerBufferSize() != 0 ? limiterProperties
                                        .getEventConsumerBufferSize() : 100));
            }
        }
    }

    /**
     * 限时
     *
     * @author maheng
     */
    @Configuration(proxyBeanMethods = false)
    public static class TimeLimiterConfiguration {
        @Bean
        @Primary
        public TimeLimiterRegistry timeLimiterRegistry(
                TimeLimiterConfigurationProperties timeLimiterConfigurationProperties,
                EventConsumerRegistry<TimeLimiterEvent> timeLimiterEventConsumerRegistry,
                RegistryEventConsumer<TimeLimiter> timeLimiterRegistryEventConsumer,
                @Qualifier("compositeTimeLimiterCustomizer") CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer) {
            TimeLimiterRegistry timeLimiterRegistry =
                    createTimeLimiterRegistry(timeLimiterConfigurationProperties, timeLimiterRegistryEventConsumer,
                            compositeTimeLimiterCustomizer);
            registerEventConsumer(timeLimiterRegistry, timeLimiterEventConsumerRegistry, timeLimiterConfigurationProperties);

            initTimeLimiterRegistry(timeLimiterRegistry, timeLimiterConfigurationProperties, compositeTimeLimiterCustomizer);
            return timeLimiterRegistry;
        }

        private TimeLimiterRegistry createTimeLimiterRegistry(
                TimeLimiterConfigurationProperties timeLimiterConfigurationProperties,
                RegistryEventConsumer<TimeLimiter> timeLimiterRegistryEventConsumer,
                CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer) {

            Map<String, TimeLimiterConfig> configs = timeLimiterConfigurationProperties.getConfigs()
                    .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> timeLimiterConfigurationProperties.createTimeLimiterConfig(
                                    entry.getKey(), entry.getValue(), compositeTimeLimiterCustomizer)));

            return new CaffeineTimeLimiterRegistry(configs, timeLimiterRegistryEventConsumer, Map.copyOf(timeLimiterConfigurationProperties.getTags()));
        }

        /**
         * Initializes the TimeLimiter registry.
         *
         * @param timeLimiterRegistry            The time limiter registry.
         * @param compositeTimeLimiterCustomizer The Composite time limiter customizer
         */
        private void initTimeLimiterRegistry(
                TimeLimiterRegistry timeLimiterRegistry,
                TimeLimiterConfigurationProperties timeLimiterConfigurationProperties,
                CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer) {

            timeLimiterConfigurationProperties.getInstances().forEach(
                    (name, properties) -> timeLimiterRegistry.timeLimiter(name,
                            timeLimiterConfigurationProperties
                                    .createTimeLimiterConfig(name, properties, compositeTimeLimiterCustomizer))
            );
        }

        /**
         * Registers the post creation consumer function that registers the consumer events to the timeLimiters.
         *
         * @param timeLimiterRegistry   The timeLimiter registry.
         * @param eventConsumerRegistry The event consumer registry.
         * @param properties            timeLimiter configuration properties
         */
        private void registerEventConsumer(TimeLimiterRegistry timeLimiterRegistry,
                                           EventConsumerRegistry<TimeLimiterEvent> eventConsumerRegistry,
                                           TimeLimiterConfigurationProperties properties) {
            timeLimiterRegistry.getEventPublisher()
                    .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry(), properties))
                    .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry(), properties));
        }

        private void registerEventConsumer(EventConsumerRegistry<TimeLimiterEvent> eventConsumerRegistry, TimeLimiter timeLimiter,
                                           TimeLimiterConfigurationProperties timeLimiterConfigurationProperties) {
            int eventConsumerBufferSize = Optional.ofNullable(timeLimiterConfigurationProperties.getInstanceProperties(timeLimiter.getName()))
                    .map(CommonTimeLimiterConfigurationProperties.InstanceProperties::getEventConsumerBufferSize)
                    .orElse(100);
            timeLimiter.getEventPublisher().onEvent(
                    eventConsumerRegistry.createEventConsumer(timeLimiter.getName(), eventConsumerBufferSize));
        }
    }
}
