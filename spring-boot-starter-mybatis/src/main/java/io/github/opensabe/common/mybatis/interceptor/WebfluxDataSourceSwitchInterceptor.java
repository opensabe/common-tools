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
package io.github.opensabe.common.mybatis.interceptor;

import java.time.Duration;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.web.filter.reactive.ServerWebExchangeContextFilter;
import org.springframework.web.server.ServerWebExchange;

import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
@Log4j2
public class WebfluxDataSourceSwitchInterceptor extends DataSourceSwitchInterceptor {

    /**
     * 在 JDBC 线程上读取 Reactor Context 的超时上限；Context 缺失或为空时会很快结束，主要用于防止异常卡死。
     */
    private static final Duration REACTOR_CONTEXT_READ_TIMEOUT = Duration.ofSeconds(2);

    public WebfluxDataSourceSwitchInterceptor() {
        // super(defaultOperId, countryProperties);
    }

    /**
     * 在<strong>当前线程</strong>从 Reactor {@link ContextView} 同步读取 operId，与即将执行 SQL 的 MyBatis
     * 线程一致，从而与 {@link DynamicRoutingDataSource} 使用的 {@link java.lang.ThreadLocal} 对齐。
     * 不可使用 {@code subscribe()} 异步回调，否则可能在 {@code configureDataSourceContext} 返回后才在其它线程执行。
     * <p>
     * 解析顺序：① {@link WebInterceptorConfiguration} 中 {@code Hooks.onEachOperator} 写入的 {@code operId}；
     * ② {@link ServerWebExchangeContextFilter} 中的请求头 {@code operId}。
     * 若当前线程不允许阻塞、Context 为空或读取失败，则返回空串，由 {@link #getCurrentOperCode(String)} 走默认 oper 映射。
     */
    private String readOperIdFromAmbientReactorContext() {
        if (log.isDebugEnabled()) {
            log.debug("WebfluxDataSourceSwitchInterceptor: synchronously resolve operId from Reactor Context on thread={}",
                    Thread.currentThread().getName());
        }
        try {
            return Mono.deferContextual(cv -> Mono.just(operIdFromContextView(cv)))
                    .block(REACTOR_CONTEXT_READ_TIMEOUT);
        } catch (Throwable ex) {
            if (log.isDebugEnabled()) {
                log.debug("WebfluxDataSourceSwitchInterceptor: could not read operId from Reactor Context, use default oper mapping", ex);
            }
            return "";
        }
    }

    /**
     * 从 Reactor Context 解析 operId：优先使用 Hook 注入的 {@code operId}，否则从当前请求的 {@link ServerWebExchange} 取请求头。
     */
    private static String operIdFromContextView(ContextView cv) {
        Object hooked = cv.getOrDefault("operId", "");
        if (hooked instanceof String s && StringUtils.isNotBlank(s)) {
            return s;
        }
        Optional<ServerWebExchange> exchange = ServerWebExchangeContextFilter.getExchange(cv);
        return exchange
                .map(ex -> ex.getRequest().getHeaders().getFirst("operId"))
                .filter(StringUtils::isNotBlank)
                .orElse("");
    }

    @Override
    public void configureDataSourceContext(BoundSql boundSql) {
        if (boundSql != null
                && StringUtils.containsIgnoreCase(boundSql.getSql().replace(" ", ""), "/*#mode=readonly*/")) {
            if (log.isDebugEnabled()) {
                log.debug("WebfluxDataSourceSwitchInterceptor.configureDataSourceContext: readonly hint, set RW=read");
            }
            DynamicRoutingDataSource.setDataSourceRW("read");
        } else if (StringUtils.isBlank(DynamicRoutingDataSource.getDataSourceRW())) {
            if (log.isDebugEnabled()) {
                log.debug("WebfluxDataSourceSwitchInterceptor.configureDataSourceContext: RW blank, set RW=write");
            }
            DynamicRoutingDataSource.setDataSourceRW("write");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("WebfluxDataSourceSwitchInterceptor.configureDataSourceContext: keep RW={}",
                        DynamicRoutingDataSource.getDataSourceRW());
            }
        }
        var dataSource = DynamicRoutingDataSource.currentDataSource();
        if (StringUtils.isNotBlank(dataSource)) {
            String code = getCurrentOperCode(dataSource);
            if (log.isDebugEnabled()) {
                log.debug(
                        "WebfluxDataSourceSwitchInterceptor.configureDataSourceContext: dataSource holder set, operFromHolder={}, countryCode={}",
                        dataSource,
                        code);
            }
            DynamicRoutingDataSource.setDataSourceCountryCode(code);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("WebfluxDataSourceSwitchInterceptor.configureDataSourceContext: no dataSource holder, resolve operId from reactor context");
            }
            String operId = readOperIdFromAmbientReactorContext();
            String code = getCurrentOperCode(operId);
            if (log.isDebugEnabled()) {
                log.debug(
                        "WebfluxDataSourceSwitchInterceptor.configureDataSourceContext: context operId={}, countryCode={}",
                        operId,
                        code);
            }
            DynamicRoutingDataSource.setDataSourceCountryCode(code);
        }
    }
}
