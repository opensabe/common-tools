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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;


@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
@Log4j2
public class WebMvcDataSourceSwitchInterceptor extends DataSourceSwitchInterceptor {


    public WebMvcDataSourceSwitchInterceptor() {
//        super( defaultOperId, countryProperties);
    }

    private static HttpServletRequest getRequest() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();

            if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                if (log.isDebugEnabled()) {
                    log.debug("WebMvcDataSourceSwitchInterceptor.getRequest: servlet request present");
                }
                return servletRequestAttributes.getRequest();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("WebMvcDataSourceSwitchInterceptor.getRequest: requestAttributes type={}",
                            requestAttributes != null ? requestAttributes.getClass().getName() : "null");
                }
                return null;
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug("WebMvcDataSourceSwitchInterceptor.getRequest: no request context, {}", e.toString());
            }
            return null;
        }
    }

    @Override
    public void configureDataSourceContext(BoundSql boundSql) {
        var request = getRequest();
        var operId = "";
        if (request != null) {
            operId = request.getHeader("operId");
            if (log.isDebugEnabled()) {
                log.debug("WebMvcDataSourceSwitchInterceptor.configureDataSourceContext: operIdHeader={}", operId);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("WebMvcDataSourceSwitchInterceptor.configureDataSourceContext: no HttpServletRequest, use default oper mapping");
            }
        }
        String countryCode = getCurrentOperCode(operId);
        if (log.isDebugEnabled()) {
            log.debug("WebMvcDataSourceSwitchInterceptor.configureDataSourceContext: resolved countryCode={}", countryCode);
        }
        DynamicRoutingDataSource.setDataSourceCountryCode(countryCode);
        if (boundSql != null
                && StringUtils.containsIgnoreCase(boundSql.getSql().replace(" ", ""), "/*#mode=readonly*/")) {
            if (log.isInfoEnabled()) {
                log.info("WebMvcDataSourceSwitchInterceptor.configureDataSourceContext: SQL hint mode=readonly, set RW=read");
            }
            DynamicRoutingDataSource.setDataSourceRW("read");
        } else if (StringUtils.isBlank(DynamicRoutingDataSource.getDataSourceRW())) {
            if (log.isDebugEnabled()) {
                log.debug("WebMvcDataSourceSwitchInterceptor.configureDataSourceContext: RW blank, set RW=write");
            }
            DynamicRoutingDataSource.setDataSourceRW("write");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("WebMvcDataSourceSwitchInterceptor.configureDataSourceContext: keep existing RW={}",
                        DynamicRoutingDataSource.getDataSourceRW());
            }
        }
    }
}
