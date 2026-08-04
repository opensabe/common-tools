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
package io.github.opensabe.spring.cloud.parent.web.common.undertow;

import io.github.opensabe.common.executor.GracefulShutdownHandler;

/**
 * Undertow 优雅关闭处理器（已废弃）。
 * <p>
 * Undertow 移除后请改用 {@link GracefulShutdownHandler}；保留本接口仅为二进制兼容。
 *
 * @deprecated 请使用 {@link GracefulShutdownHandler} 替代
 */
@Deprecated
public interface UndertowGracefulShutdownHandler extends GracefulShutdownHandler {
}
