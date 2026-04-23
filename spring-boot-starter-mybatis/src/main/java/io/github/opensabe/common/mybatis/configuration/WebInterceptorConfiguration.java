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
package io.github.opensabe.common.mybatis.configuration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

import io.github.opensabe.common.mybatis.interceptor.DataSourceSwitchInterceptor;
import io.github.opensabe.common.mybatis.interceptor.WebMvcDataSourceSwitchInterceptor;
import io.github.opensabe.common.mybatis.interceptor.WebfluxDataSourceSwitchInterceptor;
import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import io.github.opensabe.common.mybatis.webflux.WebFluxRoutingContext;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

/**
 * 根据 request header 里的 operId，自动设置数据源
 *
 * @author heng.ma
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class WebInterceptorConfiguration {


    /**
     * mvc
     *
     * @return
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public DataSourceSwitchInterceptor webmvcInterceptor() {
        return new WebMvcDataSourceSwitchInterceptor();
    }


    /**
     * WebFlux：通过 {@code contextWrite} 写入 operId，用 {@code doOnEach} 把信号上的 {@link ContextView}
     * 同步到 {@link WebFluxRoutingContext}（ThreadLocal），请求结束在 {@code doFinally} 中清理；
     * 不再使用全局 {@code Hooks.onEachOperator} / {@code OperatorEvent}，避免跨请求串扰。
     */
    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class WebfluxSupportConfiguration {

        @Bean
        public WebFilter operatorEventFilter() {
            return (exchange, chain) -> {
                String operId = StringUtils.trimToEmpty(exchange.getRequest().getHeaders().getFirst("operId"));
                Mono<Void> downstream = chain.filter(exchange)
                        .contextWrite(ctx -> ctx.put("operId", operId));
                return downstream.transformDeferredContextual((Mono<Void> mono, ContextView merged) ->
                        mono
                                // merged 为订阅时合并后的 Reactor Context（已含上文的 contextWrite(operId)），
                                // 在 doOnEach 的 signal 之前先写入，便于同请求内后续 subscribeOn/嵌套 Mono 的线程在 doOnEach 已同步过一轮。
                                .doOnSubscribe(s -> {
                                    if (merged != null && !merged.isEmpty()) {
                                        WebFluxRoutingContext.restoreContextView(merged);
                                    }
                                })
                                .doOnEach(signal -> {
                                    ContextView sigCtx = signal.getContextView();
                                    if (sigCtx != null && !sigCtx.isEmpty()) {
                                        WebFluxRoutingContext.restoreContextView(sigCtx);
                                    }
                                })
                                .doFinally(signalType -> {
                                    WebFluxRoutingContext.clear();
                                    DynamicRoutingDataSource.clear();
                                    // doFinally 与 subscribeOn 工作线程可能不同；此处清理当前信号线程上的 holder，
                                    // 并清除 RW/国家码 ThreadLocal（clear() 仅移除 dataSourceHolder）。
                                    DynamicRoutingDataSource.clearCountryCodeAndRW();
                                }));
            };
        }

        @Bean
        public DataSourceSwitchInterceptor webfluxInterceptor() {
            return new WebfluxDataSourceSwitchInterceptor();
        }
    }

}
