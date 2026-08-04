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
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import io.github.opensabe.common.config.JacksonCustomizedConfiguration;

/**
 * Jackson 模块与 JsonMapper 定制的自动配置入口。
 * <p>
 * 必须在 {@link JacksonAutoConfiguration} 之前加载，确保 {@link JacksonCustomizedConfiguration}
 * 注册的 {@code JacksonModule} / {@code JsonMapperBuilderCustomizer} 参与 Boot JsonMapper 构建。
 */
@AutoConfiguration(before = JacksonAutoConfiguration.class)
@Import(JacksonCustomizedConfiguration.class)
public class JacksonCustomizedAutoConfiguration {
}
