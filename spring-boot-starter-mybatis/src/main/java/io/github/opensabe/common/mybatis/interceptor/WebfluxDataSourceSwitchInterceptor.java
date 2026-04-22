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

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
@Log4j2
public class WebfluxDataSourceSwitchInterceptor extends DataSourceSwitchInterceptor {


    public WebfluxDataSourceSwitchInterceptor() {
//        super(defaultOperId, countryProperties);
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
            Mono.deferContextual(context -> Mono.just(context.getOrDefault("operId", "")))
                    .publishOn(Schedulers.immediate())
                    .subscribeOn(Schedulers.immediate())
                    .subscribe(operId -> {
                        String code = getCurrentOperCode(operId);
                        if (log.isDebugEnabled()) {
                            log.debug(
                                    "WebfluxDataSourceSwitchInterceptor.configureDataSourceContext: context operId={}, countryCode={}",
                                    operId,
                                    code);
                        }
                        DynamicRoutingDataSource.setDataSourceCountryCode(code);
                    });
        }
    }
}
