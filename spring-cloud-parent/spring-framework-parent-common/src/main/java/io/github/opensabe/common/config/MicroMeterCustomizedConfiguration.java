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
package io.github.opensabe.common.config;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.common.jfr.JFRObservationHandler;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

/**
 * Micrometer 观测、JFR 导出与延迟初始化 ObservationRegistry 的 Spring 配置。
 * <p>
 * Observation → Meter 由 Boot 的 {@code DefaultMeterObservationHandler} 负责；
 * LongTaskTimer 通过 {@link OpensabeMetricsEnvironmentPostProcessor} 在公用环境中固定忽略。
 */
@Configuration(proxyBeanMethods = false)
public class MicroMeterCustomizedConfiguration {

    /**
     * 将 Observation 事件写入 JFR 的处理器。
     *
     * @param generators 各 Observation 上下文对应的 JFR 事件生成器
     * @return JFR Observation 处理器
     */
    @Bean
    public JFRObservationHandler jfrTracingObservationHandler(List<ObservationToJFRGenerator<? extends Observation.Context>> generators) {
        return new JFRObservationHandler(generators);
    }

    /**
     * 延迟获取 {@link ObservationRegistry}，避免与 Boot 的 PostProcessor 初始化顺序冲突。
     *
     * @param observationRegistry Observation 注册表提供者
     * @return 统一 Observation 工厂
     */
    @Bean
    public UnifiedObservationFactory unifiedObservationFactory(ObjectProvider<ObservationRegistry> observationRegistry) {
        return new UnifiedObservationFactory(observationRegistry);
    }
}
