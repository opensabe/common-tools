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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class WebfluxDataSourceSwitchInterceptor extends DataSourceSwitchInterceptor {


    public WebfluxDataSourceSwitchInterceptor() {
//        super(defaultOperId, countryProperties);
    }

    @Override
    public void configureDataSourceContext(BoundSql boundSql) {
        if (boundSql != null
                && StringUtils.containsIgnoreCase(boundSql.getSql().replace(" ", ""), "/*#mode=readonly*/")) {
            DynamicRoutingDataSource.setDataSourceRW("read");
        } else if (StringUtils.isBlank(DynamicRoutingDataSource.getDataSourceRW())) {
            DynamicRoutingDataSource.setDataSourceRW("write");
        }
        var dataSource = DynamicRoutingDataSource.currentDataSource();
        if (StringUtils.isNotBlank(dataSource)) {
            DynamicRoutingDataSource.setDataSourceCountryCode(getCurrentOperCode(dataSource));
        } else {
            Mono.deferContextual(context -> Mono.just(context.getOrDefault("operId", "")))
                    .publishOn(Schedulers.immediate())
                    .subscribeOn(Schedulers.immediate())
                    .subscribe(operId ->
                            DynamicRoutingDataSource.setDataSourceCountryCode(getCurrentOperCode(operId)));
        }
    }
}
