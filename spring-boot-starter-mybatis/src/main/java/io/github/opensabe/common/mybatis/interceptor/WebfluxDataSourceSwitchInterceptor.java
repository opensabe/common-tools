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
import io.github.opensabe.common.mybatis.webflux.WebFluxRoutingContext;
import lombok.extern.log4j.Log4j2;
import reactor.util.context.ContextView;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
@Log4j2
public class WebfluxDataSourceSwitchInterceptor extends DataSourceSwitchInterceptor {

    public WebfluxDataSourceSwitchInterceptor() {
        // super(defaultOperId, countryProperties);
    }

    /**
     * 仅从当前线程已绑定的 {@link ContextView}（由 WebFilter 的 {@code doOnEach} 写入）以及其中的
     * {@link ServerWebExchange} 解析 operId；不创建 Mono、不调用 {@code block}、不依赖全局 Hook。
     */
    private static String resolveOperIdFromBoundContext() {
        ContextView cv = WebFluxRoutingContext.currentContextView();
        if (cv == null) {
            return "";
        }
        return operIdFromContextView(cv);
    }

    /**
     * 与无 dataSource holder 时 {@link #configureDataSourceContext} 中通过 {@code resolveOperIdFromBoundContext} 得到的结果一致
     * （尚未经 {@link #getCurrentOperCode(String)} 映射为国家码），供 WebFlux 集成测试验证 {@link WebFluxRoutingContext}。
     */
    public static String resolutionProbeOperIdFromBoundContext() {
        return resolveOperIdFromBoundContext();
    }

    /**
     * 优先使用 Context 中的 {@code operId}（由 WebFilter {@code contextWrite} 写入），
     * 否则从 {@link ServerWebExchangeContextFilter} 放入的 {@link ServerWebExchange} 读请求头。
     */
    private static String operIdFromContextView(ContextView cv) {
        Object fromContext = cv.getOrDefault("operId", "");
        if (fromContext instanceof String s && StringUtils.isNotBlank(s)) {
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
            if (log.isInfoEnabled()) {
                log.info("WebfluxDataSourceSwitchInterceptor.configureDataSourceContext: readonly hint, set RW=read");
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
                log.debug("WebfluxDataSourceSwitchInterceptor.configureDataSourceContext: no dataSource holder, resolve operId from WebFluxRoutingContext");
            }
            String operId = resolveOperIdFromBoundContext();
            String code = getCurrentOperCode(operId);
            if (log.isInfoEnabled()) {
                log.info(
                        "WebfluxDataSourceSwitchInterceptor.configureDataSourceContext: context operId={}, countryCode={}",
                        operId,
                        code);
            }
            DynamicRoutingDataSource.setDataSourceCountryCode(code);
        }
    }
}
