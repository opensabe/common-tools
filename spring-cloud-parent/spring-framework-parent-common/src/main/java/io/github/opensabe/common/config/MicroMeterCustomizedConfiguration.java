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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.common.jfr.JFRObservationHandler;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.core.instrument.observation.MeterObservationHandler;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingAwareMeterObservationHandler;

/**
 * Micrometer 观测、JFR 导出与延迟初始化 ObservationRegistry 的 Spring 配置。
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

    /**
     * 带链路追踪的 Meter Observation 处理器；省略 {@link DefaultMeterObservationHandler} 中 CPU 开销较高的 LongTaskTimer。
     * <p>
     * 仅在 classpath 存在 {@link Tracer} Bean 时注册。
     *
     * @param meterRegistry Micrometer 指标注册表
     * @param tracer        分布式追踪器
     * @return 追踪感知的 Meter Observation 处理器
     */
    @Bean
    @ConditionalOnBean(Tracer.class)
    TracingAwareMeterObservationHandler<Observation.Context> tracingAwareMeterObservationHandler(
            MeterRegistry meterRegistry, Tracer tracer) {
        MeterObservationHandler<Observation.Context> delegate = new MeterObservationHandler<>() {

            /**
             * Observation 开始时记录 Timer 采样。
             *
             * @param context 当前 Observation 上下文
             */
            @Override
            public void onStart(Observation.Context context) {
                Timer.Sample sample = Timer.start(meterRegistry);
                context.put(Timer.Sample.class, sample);
            }

            /**
             * Observation 结束时停止 Timer 并上报指标。
             *
             * @param context 当前 Observation 上下文
             */
            @Override
            public void onStop(Observation.Context context) {
                List<Tag> tags = createTags(context);
                tags.add(Tag.of("error", getErrorValue(context)));
                Timer.Sample sample = context.getRequired(Timer.Sample.class);
                sample.stop(Timer.builder(context.getName()).tags(tags).register(meterRegistry));
            }

            /**
             * Observation 事件发生时递增 Counter。
             *
             * @param event   观测事件
             * @param context 当前 Observation 上下文
             */
            @Override
            public void onEvent(Observation.Event event, Observation.Context context) {
                Counter.builder(context.getName() + "." + event.getName())
                        .tags(createTags(context))
                        .register(meterRegistry)
                        .increment();
            }

            /**
             * 从上下文中提取错误类型标签值。
             *
             * @param context 当前 Observation 上下文
             * @return 异常简单类名，无错误时返回 {@code none}
             */
            private String getErrorValue(Observation.Context context) {
                Throwable error = context.getError();
                return error != null ? error.getClass().getSimpleName() : "none";
            }

            /**
             * 将上下文中的低基数键值对转换为 Micrometer 标签列表。
             *
             * @param context 当前 Observation 上下文
             * @return 标签列表
             */
            private List<Tag> createTags(Observation.Context context) {
                List<Tag> tags = new ArrayList<>();
                for (KeyValue keyValue : context.getLowCardinalityKeyValues()) {
                    tags.add(Tag.of(keyValue.getKey(), keyValue.getValue()));
                }
                return tags;
            }
        };
        return new TracingAwareMeterObservationHandler<>(delegate, tracer);
    }
}
