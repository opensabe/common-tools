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
package io.github.opensabe.common.auto;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration;
import org.springframework.context.annotation.Import;

import io.github.opensabe.common.config.MicroMeterCustomizedConfiguration;

/**
 * Micrometer 观测与指标相关的 Spring Boot 自动配置入口。
 * <p>
 * 在 {@link ObservationAutoConfiguration} 之前加载，确保
 * {@link io.github.opensabe.common.observation.UnifiedObservationFactory}、JFR Observation 处理器
 * 等 Bean 先于默认观测链路注册。LongTaskTimer 忽略由
 * {@link io.github.opensabe.common.config.OpensabeMetricsEnvironmentPostProcessor} 在环境层固定。
 */
@AutoConfiguration(before = ObservationAutoConfiguration.class)
@Import({
        MicroMeterCustomizedConfiguration.class,
})
public class MicroMeterCustomizedAutoConfiguration {
}
