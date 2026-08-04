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

import java.time.Duration;

import org.springframework.boot.data.redis.autoconfigure.ClientResourcesBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.util.Lazy;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.lettuce.core.event.DefaultEventPublisherOptions;
import io.lettuce.core.metrics.DefaultCommandLatencyCollector;
import io.lettuce.core.metrics.DefaultCommandLatencyCollectorOptions;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

/**
 * Lettuce {@link io.lettuce.core.resource.ClientResources} 定制配置。
 * <p>
 * 为 Redis 命令注入 Micrometer 追踪、命令延迟采集与周期性延迟事件发布。
 */
@Configuration(proxyBeanMethods = false)
public class LettuceConfiguration {

    /** 未配置 {@code spring.application.name} 时使用的默认应用名。 */
    private static final String DEFAULT_APPLICATION_NAME = "application";

    /**
     * 注册 Lettuce 客户端资源构建定制器。
     *
     * @param unifiedObservationFactory 统一观测工厂，提供 {@link ObservationRegistry}
     * @param environment Spring 环境，用于读取应用名
     * @return 定制 {@link io.lettuce.core.resource.ClientResources} 的回调
     */
    @Bean
    public ClientResourcesBuilderCustomizer clientResourcesBuilderCustomizer(UnifiedObservationFactory unifiedObservationFactory, Environment environment) {
        String applicationName = environment.getProperty("spring.application.name", DEFAULT_APPLICATION_NAME);
        return builder -> builder
                .tracing(new MicrometerTracing(new LazyObservationRegistry(unifiedObservationFactory), applicationName))
                .commandLatencyRecorder(new DefaultCommandLatencyCollector(DefaultCommandLatencyCollectorOptions.builder().enable().resetLatenciesAfterEvent(true).build()))
                .commandLatencyPublisherOptions(DefaultEventPublisherOptions.builder().eventEmitInterval(Duration.ofSeconds(10)).build())
                ;
    }

    /**
     * 延迟初始化的 {@link ObservationRegistry} 包装器。
     * <p>
     * Lettuce 构建 {@link io.lettuce.core.resource.ClientResources} 时 Spring 容器可能尚未就绪，
     * 通过 {@link Lazy} 推迟对 {@link UnifiedObservationFactory#getObservationRegistry()} 的访问。
     */
    public static class LazyObservationRegistry implements ObservationRegistry {

        /** 延迟加载的真实观测注册表。 */
        private final Lazy<ObservationRegistry> observationRegistry;

        /**
         * @param unifiedObservationFactory 统一观测工厂
         */
        public LazyObservationRegistry(UnifiedObservationFactory unifiedObservationFactory) {
            this.observationRegistry = Lazy.of(unifiedObservationFactory::getObservationRegistry);
        }

        /** {@inheritDoc} */
        @Override
        public Observation getCurrentObservation() {
            return observationRegistry.get().getCurrentObservation();
        }

        /** {@inheritDoc} */
        @Override
        public Observation.Scope getCurrentObservationScope() {
            return observationRegistry.get().getCurrentObservationScope();
        }

        /** {@inheritDoc} */
        @Override
        public void setCurrentObservationScope(Observation.Scope current) {
            observationRegistry.get().setCurrentObservationScope(current);
        }

        /** {@inheritDoc} */
        @Override
        public ObservationConfig observationConfig() {
            return observationRegistry.get().observationConfig();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isNoop() {
            return observationRegistry.get().isNoop();
        }
    }

}
