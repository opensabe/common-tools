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
package io.github.opensabe.common.mybatis.plugins;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.github.opensabe.common.mybatis.interceptor.DataSourceSwitchInterceptor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

	private static TransmittableThreadLocal<String> dataSourceHolder = new TransmittableThreadLocal<>();

	private static TransmittableThreadLocal<String> dataSourceRWHolder = new TransmittableThreadLocal<>();

	private static TransmittableThreadLocal<String> dataSourceCountryCodeHolder = new TransmittableThreadLocal<>();

	public static void clear() {
		dataSourceHolder.remove();
	}

	public static void clearCountryCodeAndRW() {
		dataSourceRWHolder.remove();
		dataSourceCountryCodeHolder.remove();
	}
	public static void clearRW() {
		dataSourceRWHolder.remove();
	}

	public static String currentDataSource() {
		return dataSourceHolder.get();
	}

	public static void dataSource(String dataSource) {
		dataSourceHolder.set(dataSource);
	}

	public static void setDataSourceCountryCode(String dataSourceCountryCode) {
		dataSourceCountryCodeHolder.set(dataSourceCountryCode);
	}

	public static void setDataSourceRW(String dataSourceRW) {
		dataSourceRWHolder.set(dataSourceRW);
	}
	public static String getDataSourceRW() {
		return dataSourceRWHolder.get();
	}

	/**
	 * 根据DataSourceName和Read/Write进行数据源路由的Map，其value为AbstractRoutingDataSource中的TargetDataSources的Key
	 */
	private Map<String, Map<String, List<String>>> dataSourceIndexMap = new HashMap<>();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DynamicRoutingDataSource(String defaultClusterName, Map dataSourceMap,
			Map<String, Map<String, List<String>>> dataSourceIndexMap) {
		String defaultIndex = resolveDefaultIndex(dataSourceIndexMap, defaultClusterName);
		setTargetDataSources(dataSourceMap);
		setDefaultTargetDataSource(defaultIndex != null ? dataSourceMap.get(defaultIndex) : null);
		this.dataSourceIndexMap = dataSourceIndexMap;
		afterPropertiesSet();
	}

	/**
	 * According to Country Code and R/W, select a random datasource to execute the
	 * SQL
	 */
	@Override
	protected Object determineCurrentLookupKey() {
		log.info("determine datasource:{},{}", dataSourceCountryCodeHolder.get(), dataSourceRWHolder.get());
		log.info("dataSourceIndexMap:{}", dataSourceIndexMap.toString());
		var country = dataSourceIndexMap.get(dataSourceCountryCodeHolder.get());
		if (MapUtils.isEmpty(country)) {
			//如果没有配置国家就取默认的国际，因为Publuc项目只配置一个国家，对应operId没有，
			//如果直接取默认的数据源，这时候就不走从库了
			country = dataSourceIndexMap.get(DataSourceSwitchInterceptor.getDefaultCountryCode());
		}
		if (MapUtils.isEmpty(country)) {
			return null;
		}
		List<String> dataSourceKeys = country.get(dataSourceRWHolder.get());

		if (CollectionUtils.isEmpty(dataSourceKeys)) {
			return null;
		}
		String index = dataSourceKeys.get(RandomUtils.nextInt(0, dataSourceKeys.size()));
 		log.info("choose datasource: {}", index);
		return index;
	}

	private String resolveDefaultIndex(Map<String, Map<String, List<String>>> dataSourceIndexMap,
			String defaultClusterName) {
		Map<String, List<String>> rwIndexMap = dataSourceIndexMap.get(defaultClusterName);
		if (rwIndexMap != null) {
			if (rwIndexMap.get("write") != null && rwIndexMap.get("write").size() > 0) {
				return rwIndexMap.get("write").get(0);
			} else if (rwIndexMap.get("read") != null && rwIndexMap.get("read").size() > 0) {
				return rwIndexMap.get("read").get(0);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
