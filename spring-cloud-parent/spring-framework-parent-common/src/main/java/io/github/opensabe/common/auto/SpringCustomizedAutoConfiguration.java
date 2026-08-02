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
import org.springframework.context.annotation.Import;

import io.github.opensabe.common.config.JacksonCustomizedConfiguration;
import io.github.opensabe.common.config.SpringCommonUtilConfiguration;

/**
 * 通用 Spring 定制自动配置入口。
 * <p>
 * 聚合 {@link SpringCommonUtilConfiguration} 与 {@link JacksonCustomizedConfiguration}，
 * 注册工具类 Bean、密钥管理与 Jackson 模块等基础设施。
 */
@AutoConfiguration
@Import({
        SpringCommonUtilConfiguration.class,
        JacksonCustomizedConfiguration.class,
})
public class SpringCustomizedAutoConfiguration {
}
