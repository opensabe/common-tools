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
package io.github.opensabe.common.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * common-utils 自动配置占位。
 * <p>
 * {@link io.github.opensabe.common.config.SpringCommonUtilConfiguration} 已由
 * {@code spring-framework-parent-common} 的
 * {@link io.github.opensabe.common.auto.SpringCustomizedAutoConfiguration} 注册，
 * 此处不再重复 {@code @Import}，避免双重装配。
 *
 * @deprecated 无独立 Bean；保留类名以免破坏外部对 auto-config 入口的引用，后续小版本可删除。
 */
@Deprecated(since = "3.0.0", forRemoval = true)
@AutoConfiguration
public class CommonUtilAutoConfiguration {
}
