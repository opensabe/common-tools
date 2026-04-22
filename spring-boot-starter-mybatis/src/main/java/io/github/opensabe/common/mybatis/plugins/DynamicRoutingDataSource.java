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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import io.github.opensabe.common.mybatis.interceptor.DataSourceSwitchInterceptor;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> dataSourceHolder = new ThreadLocal<>();

    private static final ThreadLocal<String> dataSourceRWHolder = new ThreadLocal<>();

    private static final ThreadLocal<String> dataSourceCountryCodeHolder = new ThreadLocal<>();
    /**
     * 根据DataSourceName和Read/Write进行数据源路由的Map，其value为AbstractRoutingDataSource中的TargetDataSources的Key
     */
    private Map<String, Map<String, List<String>>> dataSourceIndexMap = new HashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public DynamicRoutingDataSource(String defaultClusterName, Map dataSourceMap,
                                    Map<String, Map<String, List<String>>> dataSourceIndexMap) {
        String defaultIndex = resolveDefaultIndex(dataSourceIndexMap, defaultClusterName);
        setTargetDataSources(dataSourceMap);
        setDefaultTargetDataSource(defaultIndex != null ? dataSourceMap.get(defaultIndex) : null);
        this.dataSourceIndexMap = dataSourceIndexMap;
        if (log.isDebugEnabled()) {
            log.debug(
                    "DynamicRoutingDataSource ctor: defaultClusterName={}, dataSourceMapSize={}, dataSourceIndexMapKeys={}",
                    defaultClusterName,
                    dataSourceMap != null ? dataSourceMap.size() : 0,
                    dataSourceIndexMap != null ? dataSourceIndexMap.keySet() : null);
        }
        afterPropertiesSet();
    }

    public static void clear() {
        if (log.isDebugEnabled()) {
            log.debug("DynamicRoutingDataSource.clear: removing dataSourceHolder, previous={}", dataSourceHolder.get());
        }
        dataSourceHolder.remove();
    }

    public static void clearCountryCodeAndRW() {
        if (log.isDebugEnabled()) {
            log.debug(
                    "DynamicRoutingDataSource.clearCountryCodeAndRW: rw={}, countryCode={}",
                    dataSourceRWHolder.get(),
                    dataSourceCountryCodeHolder.get());
        }
        dataSourceRWHolder.remove();
        dataSourceCountryCodeHolder.remove();
    }

    public static void clearRW() {
        if (log.isDebugEnabled()) {
            log.debug("DynamicRoutingDataSource.clearRW: previous={}", dataSourceRWHolder.get());
        }
        dataSourceRWHolder.remove();
    }

    public static String currentDataSource() {
        return dataSourceHolder.get();
    }

    public static void dataSource(String dataSource) {
        if (log.isDebugEnabled()) {
            log.debug("DynamicRoutingDataSource.dataSource: set={}", dataSource);
        }
        dataSourceHolder.set(dataSource);
    }

    public static void setDataSourceCountryCode(String dataSourceCountryCode) {
        if (log.isDebugEnabled()) {
            log.debug("DynamicRoutingDataSource.setDataSourceCountryCode: {}", dataSourceCountryCode);
        }
        dataSourceCountryCodeHolder.set(dataSourceCountryCode);
    }

    public static String getDataSourceRW() {
        return dataSourceRWHolder.get();
    }

    public static void setDataSourceRW(String dataSourceRW) {
        if (log.isDebugEnabled()) {
            log.debug("DynamicRoutingDataSource.setDataSourceRW: {}", dataSourceRW);
        }
        dataSourceRWHolder.set(dataSourceRW);
    }

    /**
     * According to Country Code and R/W, select a random datasource to execute the
     * SQL
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String countryCode = dataSourceCountryCodeHolder.get();
        String rw = dataSourceRWHolder.get();
        if (log.isDebugEnabled()) {
            log.debug(
                    "DynamicRoutingDataSource.determineCurrentLookupKey: countryCode={}, rw={}, dataSourceIndexMap={}",
                    countryCode,
                    rw,
                    dataSourceIndexMap);
        }
        var country = dataSourceIndexMap.get(countryCode);
        if (MapUtils.isEmpty(country)) {
            //如果没有配置国家就取默认的国际，因为Publuc项目只配置一个国家，对应operId没有，
            //如果直接取默认的数据源，这时候就不走从库了
            String fallback = DataSourceSwitchInterceptor.getDefaultCountryCode();
            if (log.isDebugEnabled()) {
                log.debug(
                        "DynamicRoutingDataSource.determineCurrentLookupKey: empty country for code={}, fallbackDefaultCountryCode={}",
                        countryCode,
                        fallback);
            }
            country = dataSourceIndexMap.get(fallback);
        }
        if (MapUtils.isEmpty(country)) {
            if (log.isDebugEnabled()) {
                log.debug("DynamicRoutingDataSource.determineCurrentLookupKey: no country map after fallback, return null");
            }
            return null;
        }
        List<String> dataSourceKeys = country.get(rw);

        if (CollectionUtils.isEmpty(dataSourceKeys)) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "DynamicRoutingDataSource.determineCurrentLookupKey: no datasource keys for rw={}, countryKeys={}",
                        rw,
                        country.keySet());
            }
            return null;
        }
        String index = dataSourceKeys.get(RandomUtils.nextInt(0, dataSourceKeys.size()));
        if (log.isDebugEnabled()) {
            log.debug(
                    "DynamicRoutingDataSource.determineCurrentLookupKey: chose index={}, poolSize={}",
                    index,
                    dataSourceKeys.size());
        }
        return index;
    }

    private String resolveDefaultIndex(Map<String, Map<String, List<String>>> dataSourceIndexMap,
                                       String defaultClusterName) {
        Map<String, List<String>> rwIndexMap = dataSourceIndexMap.get(defaultClusterName);
        if (rwIndexMap != null) {
            if (rwIndexMap.get("write") != null && rwIndexMap.get("write").size() > 0) {
                String idx = rwIndexMap.get("write").get(0);
                if (log.isDebugEnabled()) {
                    log.debug("DynamicRoutingDataSource.resolveDefaultIndex: cluster={}, using first write={}",
                            defaultClusterName, idx);
                }
                return idx;
            } else if (rwIndexMap.get("read") != null && rwIndexMap.get("read").size() > 0) {
                String idx = rwIndexMap.get("read").get(0);
                if (log.isDebugEnabled()) {
                    log.debug("DynamicRoutingDataSource.resolveDefaultIndex: cluster={}, no write, using first read={}",
                            defaultClusterName, idx);
                }
                return idx;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "DynamicRoutingDataSource.resolveDefaultIndex: cluster={} has rwIndexMap but no read/write keys",
                            defaultClusterName);
                }
                return null;
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("DynamicRoutingDataSource.resolveDefaultIndex: no rwIndexMap for cluster={}", defaultClusterName);
            }
            return null;
        }
    }
}
