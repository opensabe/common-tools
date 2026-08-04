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
package io.github.opensabe.common.executor;

import org.springframework.core.Ordered;

/**
 * 应用上下文关闭时触发的优雅停机 SPI。
 * <p>
 * 实现类在嵌入式 Web 服务器开始关闭等阶段被调用，可通过 {@link Ordered#getOrder()} 控制执行顺序。
 * 原 {@code UndertowGracefulShutdownHandler} 已弃用，请实现本接口。
 *
 * @see org.springframework.core.Ordered
 */
public interface GracefulShutdownHandler extends Ordered {

    /**
     * 执行资源排空与关闭逻辑（如线程池 shutdown、连接 drain 等）。
     */
    void gracefullyShutdown();
}
