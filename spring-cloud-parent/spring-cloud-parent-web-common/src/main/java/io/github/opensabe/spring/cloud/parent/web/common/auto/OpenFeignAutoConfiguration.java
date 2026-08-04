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
package io.github.opensabe.spring.cloud.parent.web.common.auto;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import io.github.opensabe.spring.cloud.parent.web.common.config.CommonOpenFeignConfiguration;

/**
 * OpenFeign 自动配置入口。
 * <p>
 * 导入 {@link io.github.opensabe.spring.cloud.parent.web.common.config.CommonOpenFeignConfiguration}，
 * 为所有 Feign 客户端注入默认配置、Resilience4j 装饰与 JFR 观测等能力。
 */
@AutoConfiguration
@Import({CommonOpenFeignConfiguration.class})
//@EnableFeignClients(value = "io.github.opensabe", defaultConfiguration = DefaultOpenFeignConfiguration.class)
public class OpenFeignAutoConfiguration {


}
