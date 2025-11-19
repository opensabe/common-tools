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

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.AutoMappingUnknownColumnBehavior;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import tk.mybatis.mapper.autoconfigure.MybatisProperties;

import java.util.List;
import java.util.Objects;

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
                } else {
                    o.setAutoMappingBehavior(AutoMappingBehavior.FULL);
                }

                if (Objects.nonNull(configuration.getAutoMappingUnknownColumnBehavior())) {
                    o.setAutoMappingUnknownColumnBehavior(configuration.getAutoMappingUnknownColumnBehavior());
                } else  {
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

                // 补充的配置属性
                if (Objects.nonNull(configuration.getMultipleResultSetsEnabled())) {
                    o.setMultipleResultSetsEnabled(configuration.getMultipleResultSetsEnabled());
                }

                if (Objects.nonNull(configuration.getUseGeneratedKeys())) {
                    o.setUseGeneratedKeys(configuration.getUseGeneratedKeys());
                }

                if (Objects.nonNull(configuration.getUseColumnLabel())) {
                    o.setUseColumnLabel(configuration.getUseColumnLabel());
                }

                if (Objects.nonNull(configuration.getUseActualParamName())) {
                    o.setUseActualParamName(configuration.getUseActualParamName());
                }

                if (Objects.nonNull(configuration.getReturnInstanceForEmptyRow())) {
                    o.setReturnInstanceForEmptyRow(configuration.getReturnInstanceForEmptyRow());
                }

                if (Objects.nonNull(configuration.getShrinkWhitespacesInSql())) {
                    o.setShrinkWhitespacesInSql(configuration.getShrinkWhitespacesInSql());
                }

                if (Objects.nonNull(configuration.getNullableOnForEach())) {
                    o.setNullableOnForEach(configuration.getNullableOnForEach());
                }

                if (Objects.nonNull(configuration.getArgNameBasedConstructorAutoMapping())) {
                    o.setArgNameBasedConstructorAutoMapping(configuration.getArgNameBasedConstructorAutoMapping());
                }

                if (Objects.nonNull(configuration.getLocalCacheScope())) {
                    o.setLocalCacheScope(configuration.getLocalCacheScope());
                }

                if (Objects.nonNull(configuration.getDefaultResultSetType())) {
                    o.setDefaultResultSetType(configuration.getDefaultResultSetType());
                }

                if (Objects.nonNull(configuration.getDefaultExecutorType())) {
                    o.setDefaultExecutorType(configuration.getDefaultExecutorType());
                }

                if (Objects.nonNull(configuration.getDefaultStatementTimeout())) {
                    o.setDefaultStatementTimeout(configuration.getDefaultStatementTimeout());
                }

                if (Objects.nonNull(configuration.getDefaultFetchSize())) {
                    o.setDefaultFetchSize(configuration.getDefaultFetchSize());
                }

                if (Objects.nonNull(configuration.getVfsImpl())) {
                    o.setVfsImpl(configuration.getVfsImpl());
                }

                if (Objects.nonNull(configuration.getDefaultSqlProviderType())) {
                    o.setDefaultSqlProviderType(configuration.getDefaultSqlProviderType());
                }

                if (Objects.nonNull(configuration.getConfigurationFactory())) {
                    o.setConfigurationFactory(configuration.getConfigurationFactory());
                }

                if (Objects.nonNull(configuration.getVariables())) {
                    o.setVariables(configuration.getVariables());
                }

                if (Objects.nonNull(configuration.getDatabaseId())) {
                    o.setDatabaseId(configuration.getDatabaseId());
                }

			});
		}
	}
}