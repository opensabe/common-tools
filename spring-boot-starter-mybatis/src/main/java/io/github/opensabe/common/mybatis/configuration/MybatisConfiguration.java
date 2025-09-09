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

import java.util.List;
import java.util.Objects;

import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.AutoMappingUnknownColumnBehavior;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import tk.mybatis.mapper.autoconfigure.MybatisProperties;

@Log4j2
@Configuration(proxyBeanMethods = false)
public class MybatisConfiguration {
    @Autowired
    private List<SqlSessionFactory> sqlSessionFactories;

    @Autowired
    private MybatisProperties mybatisProperties;

    @PostConstruct
    public void afterProperties() {
        MybatisProperties.CoreConfiguration configuration = mybatisProperties.getConfiguration();
        if (configuration != null) {
            sqlSessionFactories.forEach(s -> {
                log.info("set configuration of sqlSessionFactory {} -> {}", s.getClass(), configuration.getClass());
                org.apache.ibatis.session.Configuration o = s.getConfiguration();

                if (Objects.nonNull(configuration.getDefaultEnumTypeHandler())) {
                    o.setDefaultEnumTypeHandler(configuration.getDefaultEnumTypeHandler());
                }

                if (Objects.nonNull(configuration.getMapUnderscoreToCamelCase())) {
                    o.setMapUnderscoreToCamelCase(configuration.getMapUnderscoreToCamelCase());
                }

                if (Objects.nonNull(configuration.getAggressiveLazyLoading())) {
                    o.setAggressiveLazyLoading(configuration.getAggressiveLazyLoading());
                }

                if (Objects.nonNull(configuration.getAutoMappingBehavior())) {
                    o.setAutoMappingBehavior(configuration.getAutoMappingBehavior());
                }

                o.setAutoMappingBehavior(configuration.getAutoMappingBehavior());

                if (Objects.isNull(o.getAutoMappingBehavior())) {
                    o.setAutoMappingBehavior(AutoMappingBehavior.FULL);
                }


                if (Objects.nonNull(configuration.getAutoMappingUnknownColumnBehavior())) {
                    o.setAutoMappingUnknownColumnBehavior(configuration.getAutoMappingUnknownColumnBehavior());
                }

                if (Objects.isNull(o.getAutoMappingUnknownColumnBehavior())) {
                    o.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.NONE);
                }


                if (Objects.nonNull(configuration.getCacheEnabled())) {
                    o.setCacheEnabled(configuration.getCacheEnabled());
                }


                if (Objects.nonNull(configuration.getCallSettersOnNulls())) {
                    o.setCallSettersOnNulls(configuration.getCallSettersOnNulls());
                }

                if (Objects.nonNull(configuration.getLazyLoadingEnabled())) {
                    o.setLazyLoadingEnabled(configuration.getLazyLoadingEnabled());
                }
                if (Objects.nonNull(configuration.getLazyLoadTriggerMethods())) {
                    o.setLazyLoadTriggerMethods(configuration.getLazyLoadTriggerMethods());
                }
                if (Objects.nonNull(configuration.getLogImpl())) {
                    o.setLogImpl(configuration.getLogImpl());
                }
                if (Objects.nonNull(configuration.getJdbcTypeForNull())) {
                    o.setJdbcTypeForNull(configuration.getJdbcTypeForNull());
                }
                if (Objects.nonNull(configuration.getLogPrefix())) {
                    o.setLogPrefix(configuration.getLogPrefix());
                }

                if (Objects.nonNull(configuration.getSafeRowBoundsEnabled())) {
                    o.setSafeRowBoundsEnabled(configuration.getSafeRowBoundsEnabled());
                }
                if (Objects.nonNull(configuration.getSafeResultHandlerEnabled())) {
                    o.setSafeResultHandlerEnabled(configuration.getSafeResultHandlerEnabled());
                }


            });
        }
    }
}