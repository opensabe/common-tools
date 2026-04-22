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
package io.github.opensabe.common.mybatis.webflux;

import reactor.util.context.ContextView;

/**
 * 将当前 Reactor 信号上的 {@link ContextView} 暴露给阻塞式 MyBatis 插件（同线程读取），
 * 避免在拦截器里 {@code Mono.deferContextual().block()} 或全局 {@code Hooks.onEachOperator}。
 * <p>
 * 由 {@link io.github.opensabe.common.mybatis.configuration.WebInterceptorConfiguration} 中
 * WebFilter 的 {@code doOnEach} 写入、{@code doFinally} 与请求结束清理；与
 * {@link io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource} 的 ThreadLocal 配合使用。
 */
public final class WebFluxRoutingContext {

    private static final ThreadLocal<ContextView> CONTEXT_VIEW = new ThreadLocal<>();

    private WebFluxRoutingContext() {
    }

    /**
     * 绑定当前 Reactor 信号携带的 Context（由 WebFilter 的 doOnEach 调用，须与后续 JDBC 同线程或已由框架传播到该线程）。
     */
    public static void restoreContextView(ContextView contextView) {
        CONTEXT_VIEW.set(contextView);
    }

    /**
     * 供 MyBatis 拦截器读取；若不在 WebFlux 请求链或尚未收到带 Context 的信号，可能为 null。
     */
    public static ContextView currentContextView() {
        return CONTEXT_VIEW.get();
    }

    public static void clear() {
        CONTEXT_VIEW.remove();
    }
}
