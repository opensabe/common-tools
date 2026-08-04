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
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Import;

import io.github.opensabe.common.config.ExceptionConfiguration;
import io.github.opensabe.common.config.ExtendValidatorConfigure;

/**
 * Bean Validation 与全局异常处理相关的 Spring Boot 自动配置入口。
 * <p>
 * 在 Spring Boot 默认 {@link org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration}
 * 之前加载，以便注册扩展约束校验器与统一异常处理器。
 */
@AutoConfiguration
@Import({ExtendValidatorConfigure.class, ExceptionConfiguration.class})
@AutoConfigureBefore(org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration.class)
public class ValidationAutoConfiguration {
}
