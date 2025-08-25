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

import com.github.pagehelper.Dialect;
import com.github.pagehelper.PageException;
import com.github.pagehelper.cache.Cache;
import com.github.pagehelper.cache.CacheFactory;
import com.github.pagehelper.util.ExecutorUtil;
import com.github.pagehelper.util.MSUtils;
import com.github.pagehelper.util.StringUtil;
import io.github.opensabe.common.mybatis.configuration.SqlSessionFactoryConfiguration;
import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import io.github.opensabe.common.mybatis.properties.CountryProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public abstract class DataSourceSwitchInterceptor implements Interceptor{


	private volatile Dialect dialect;
	private String countSuffix = "_COUNT";
	protected Cache<String, MappedStatement> msCountMap = null;
	private String default_dialect_class = "com.github.pagehelper.PageHelper";





	private static String getDefaultOperId () {
		return SqlSessionFactoryConfiguration.defaultOperId;
	}

	private static CountryProperties getCountryProperties () {
		return SqlSessionFactoryConfiguration.countryProperties;
	}



	public static String getDefaultCountryCode () {
		return getCountryProperties().getMap().get(getDefaultOperId());
	}


	public String getCurrentOperCode(String operId) {
		if (StringUtils.isBlank(operId)) {
			operId = getDefaultOperId();
		}
		return getCountryProperties().getMap().get(operId);
	}

	/**
	 * Spring bean 方式配置时，如果没有配置属性就不会执行下面的 setProperties 方法，就不会初始化
	 * <p>
	 * 因此这里会出现 null 的情况 fixed #26
	 */
	private void checkDialectExists() {
		if (dialect == null) {
			synchronized (default_dialect_class) {
				if (dialect == null) {
					setProperties(new Properties());
				}
			}
		}
	}

	/*
	 * Set the countryCode and R/W attribute (according to whether SQL contains
	 * mode=readonly) for datasource lookup
	 */
//	private void configureDataSourceContext(BoundSql boundSql) {
//		OperIdUtil operIdUtil = applicationContext.getBean(OperIdUtil.class);
//		DynamicRoutingDataSource
//				.setDataSourceCountryCode(operIdUtil.getCurrentOperCode());
//		if (boundSql != null
//				&& StringUtils.containsIgnoreCase(boundSql.getSql().replace(" ", ""), "/*#mode=readonly*/")) {
//			DynamicRoutingDataSource.setDataSourceRW("read");
//		} else if (StringUtils.isBlank(DynamicRoutingDataSource.getDataSourceRW())){
//			DynamicRoutingDataSource.setDataSourceRW("write");
//		}
//	}
	public abstract void configureDataSourceContext(BoundSql boundSql);

	@SuppressWarnings("rawtypes")
	private Long count(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
			ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
		String countMsId = ms.getId() + countSuffix;
		Long count;
		// 先判断是否存在手写的 count 查询
		MappedStatement countMs = ExecutorUtil.getExistedMappedStatement(ms.getConfiguration(), countMsId);
		if (countMs != null) {
			// DynamicRoutingDataSource.dataSource(dataSource);
			count = ExecutorUtil.executeManualCount(executor, countMs, parameter, boundSql, resultHandler);
		} else {
			countMs = msCountMap.get(countMsId);
			// 自动创建
			if (countMs == null) {
				// 根据当前的 ms 创建一个返回值为 Long 类型的 ms
				countMs = MSUtils.newCountMappedStatement(ms, countMsId);
				msCountMap.put(countMsId, countMs);
			}
			// DynamicRoutingDataSource.dataSource(dataSource);
			count = ExecutorUtil.executeAutoCount(dialect, executor, countMs, parameter, boundSql, rowBounds,
					resultHandler);
		}
		return count;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		// dataSource = DynamicRoutingDataSource.currentDataSource();
		try {
			Object[] args = invocation.getArgs();
			if (args.length == 2) {
				configureDataSourceContext(null);
				return invocation.proceed();
			}
			MappedStatement ms = (MappedStatement) args[0];
			Object parameter = args[1];
			RowBounds rowBounds = (RowBounds) args[2];
			ResultHandler resultHandler = (ResultHandler) args[3];
			Executor executor = (Executor) invocation.getTarget();
			CacheKey cacheKey;
			BoundSql boundSql;
			// 由于逻辑关系，只会进入一次
			if (args.length == 4) {
				// 4 个参数时
				boundSql = ms.getBoundSql(parameter);
				cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
			} else {
				// 6 个参数时
				cacheKey = (CacheKey) args[4];
				boundSql = (BoundSql) args[5];
			}
			configureDataSourceContext(boundSql);
			checkDialectExists();
			List resultList;
			// 调用方法判断是否需要进行分页，如果不需要，直接返回结果
			if (!dialect.skip(ms, parameter, rowBounds)) {
				// 判断是否需要进行 count 查询
				if (dialect.beforeCount(ms, parameter, rowBounds)) {
					// 查询总数
					Long count = count(executor, ms, parameter, rowBounds, resultHandler, boundSql);
					// 处理查询总数，返回 true 时继续分页查询，false 时直接返回
					if (!dialect.afterCount(count, parameter, rowBounds)) {
						// 当查询总数为 0 时，直接返回空的结果
						return dialect.afterPage(new ArrayList(), parameter, rowBounds);
					}
				}
				resultList = ExecutorUtil.pageQuery(dialect, executor, ms, parameter, rowBounds, resultHandler,
						boundSql, cacheKey);
				return dialect.afterPage(resultList, parameter, rowBounds);
			} else {
				// rowBounds用参数值，不使用分页插件处理时，仍然支持默认的内存分页
				return invocation.proceed();
			}
		} finally {
			if (dialect != null) {
				dialect.afterAll();
			}
				clear();
			// dataSource = null;
		}
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		// 缓存 count ms
		msCountMap = CacheFactory.createCache(properties.getProperty("msCountCache"), "ms", properties);
		String dialectClass = properties.getProperty("dialect");
		if (StringUtil.isEmpty(dialectClass)) {
			dialectClass = default_dialect_class;
		}
		try {
			Class<?> aClass = Class.forName(dialectClass);
			dialect = (Dialect) aClass.getDeclaredConstructor().newInstance();
		} catch (Throwable e) {
			throw new PageException(e);
		}
		dialect.setProperties(properties);

		String countSuffix = properties.getProperty("countSuffix");
		if (StringUtil.isNotEmpty(countSuffix)) {
			this.countSuffix = countSuffix;
		}
	}

	protected void clear () {
		DynamicRoutingDataSource.clearCountryCodeAndRW();
	}
}
