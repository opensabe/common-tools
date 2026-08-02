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
package io.github.opensabe.spring.cloud.parent.common.auto;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import io.github.opensabe.spring.cloud.parent.common.config.ShutdownOptimizeConfiguration;

/**
 * 优雅关闭优化自动配置入口。
 * <p>
 * 注册 Eureka 下线后的延迟缓冲监听器，减少关闭窗口内的 503 与线程池拒绝。
 */
@AutoConfiguration
@Import(ShutdownOptimizeConfiguration.class)
public class ShutdownOptimizeAutoConfiguration {
}
