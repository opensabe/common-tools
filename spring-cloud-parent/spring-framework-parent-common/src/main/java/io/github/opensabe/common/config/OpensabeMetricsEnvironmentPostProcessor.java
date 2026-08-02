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

import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * 为本库消费方写入 Micrometer Observation 相关默认指标配置。
 * <p>
 * Boot 4 / Micrometer 1.17 的 {@code DefaultMeterObservationHandler} 默认仍会为每个
 * Observation 创建 {@code LongTaskTimer}（{@code *.active}），CPU 开销较高，且在未
 * {@code stop} 时可能滞留 Sample。此处固定忽略 {@code LONG_TASK_TIMER}，等价于官方：
 * {@code management.metrics.observations.ignored-meters=long_task_timer}。
 * <p>
 * 实现 {@link org.springframework.boot.EnvironmentPostProcessor}（Boot 4 新包；
 * {@code org.springframework.boot.env.EnvironmentPostProcessor} 已 deprecated forRemoval）。
 * 通过 {@code META-INF/spring.factories} 的
 * {@code org.springframework.boot.EnvironmentPostProcessor} 键注册。
 * <p>
 * 使用最低优先级 {@link MapPropertySource}：应用若显式配置同名属性仍可覆盖。
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class OpensabeMetricsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    /**
     * Boot 绑定到 {@code MetricsProperties.Observations#ignoredMeters} 的配置键。
     */
    public static final String IGNORED_METERS_PROPERTY = "management.metrics.observations.ignored-meters";

    /**
     * 对应 {@code DefaultMeterObservationHandler.IgnoredMeters.LONG_TASK_TIMER} 的宽松绑定值。
     */
    public static final String LONG_TASK_TIMER = "long_task_timer";

    /**
     * 本默认配置的 PropertySource 名称。
     */
    public static final String PROPERTY_SOURCE_NAME = "opensabeMetricsDefaults";

    /**
     * 在环境中补充忽略 LongTaskTimer 的默认配置（若应用尚未设置该键）。
     *
     * @param environment 当前可配置环境
     * @param application 正在启动的 Spring Boot 应用
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getPropertySources().contains(PROPERTY_SOURCE_NAME)) {
            return;
        }
        if (environment.containsProperty(IGNORED_METERS_PROPERTY)) {
            return;
        }
        environment.getPropertySources().addLast(new MapPropertySource(
                PROPERTY_SOURCE_NAME,
                Map.of(IGNORED_METERS_PROPERTY, LONG_TASK_TIMER)));
    }
}
