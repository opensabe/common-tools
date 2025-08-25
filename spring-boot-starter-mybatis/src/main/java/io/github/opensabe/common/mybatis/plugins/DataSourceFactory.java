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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.springframework.beans.BeanUtils;

import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.alibaba.druid.filter.logging.LogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.alibaba.druid.wall.spi.MySqlWallProvider;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.github.opensabe.common.mybatis.interceptor.ConnectionObservationFilter;
import io.github.opensabe.common.mybatis.properties.DataSourceProperties;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * 数据源工厂
 *
 * @author maheng
 */
@Log4j2
public class DataSourceFactory {
    private static void addDataSourceToIndexAndReturn(Map<String, DataSource> dataSourceMap,
                                                      Map<String, List<String>> rwIndexMap, DataSourceProperties properties, DataSource dataSource,
                                                      String rwKeyword, int num) {
        if (!rwIndexMap.containsKey(rwKeyword)) {
            rwIndexMap.put(rwKeyword, new ArrayList<>());
        }
        String index = properties.getClusterName() + "-" + rwKeyword + "-" + num;
        rwIndexMap.get(rwKeyword).add(index);
        dataSourceMap.put(index, dataSource);
    }

    /**
     * 批量创建数据源并创建数据源索引
     *
     * @param dataSourceProperties 数据源配置
     * @param dataSourceIndexMap   数据源索引Map
     * @return k 数据源名称，v数据源
     */
    private static Map<String, DataSource> batchCreate(List<DataSourceProperties> dataSourceProperties,
                                                       Map<String, Map<String, List<String>>> dataSourceIndexMap) {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        for (int num = 0; num < dataSourceProperties.size(); num++) {
            DataSourceProperties properties = dataSourceProperties.get(num);
            DataSource dataSource = create(properties);

            if (!dataSourceIndexMap.containsKey(properties.getClusterName())) {
                dataSourceIndexMap.put(properties.getClusterName(), new HashMap<>());
            }

            if (properties.getIsWriteAllowed()) {
                addDataSourceToIndexAndReturn(dataSourceMap, dataSourceIndexMap.get(properties.getClusterName()),
                        properties, dataSource, "write", num);
            } else {
                addDataSourceToIndexAndReturn(dataSourceMap, dataSourceIndexMap.get(properties.getClusterName()),
                        properties, dataSource, "read", num);
            }
        }
        return dataSourceMap;
    }

    /**
     * 通过数据源配置创建
     *
     * @param properties 数据源配置
     * @return Druid数据源
     * @throws SQLException
     */
    @SneakyThrows
    private static DataSource create(DataSourceProperties properties) {
        log.info("init datasource:{},{},{}", properties.getName(), properties.getClusterName(), properties.getUrl());
        DruidDataSource dataSource = new DruidDataSource();
        BeanUtils.copyProperties(properties, dataSource);

        LogFilter logFilter = new Log4j2Filter();
        BeanUtils.copyProperties(properties, logFilter);

        StatFilter statFilter = new StatFilter() {
            private final LoadingCache<String, AtomicInteger> cache
                    = Caffeine.newBuilder()
                    .expireAfterWrite(properties.getAlarmIntervalInSeconds(), TimeUnit.SECONDS)
                    .build(k -> new AtomicInteger());

            @Override
            protected void handleSlowSql(StatementProxy statementProxy) {
                /**
                 * 聚合慢 SQL。在某一界限再 FATAL 报警
                 */
                String lastExecuteSql = statementProxy.getLastExecuteSql();
                int i = cache.get(lastExecuteSql).incrementAndGet();
                if (i == properties.getAlarmThreshold()) {
                    cache.get(lastExecuteSql).set(0);
                    String slowParameters = buildSlowParameters(statementProxy);
                    final long nowNano = System.nanoTime();
                    final long nanos = nowNano - statementProxy.getLastExecuteStartNano();
                    long millis = nanos / (1000 * 1000);
                    log.fatal(
                            "Slow SQL {} times in {}s, SQL: {}, last execution time: {}ms, parameters: {}",
                            properties.getAlarmThreshold(), properties.getAlarmIntervalInSeconds(),
                            lastExecuteSql, millis, slowParameters);
                } else {
                    log.info("Slow SQL {} times in {}s, SQL: {}", i, properties.getAlarmIntervalInSeconds(), lastExecuteSql);
                }
            }
        };
        BeanUtils.copyProperties(properties, statFilter);

        WallFilter wallFilter = new WallFilter();
        WallConfig wallConfig = new WallConfig(MySqlWallProvider.DEFAULT_CONFIG_DIR);
        wallFilter.setLogViolation(properties.isLogViolation());
        wallFilter.setThrowException(properties.isThrowException());
        wallFilter.setConfig(wallConfig);
        BeanUtils.copyProperties(properties, wallFilter.getConfig());

        ConnectionObservationFilter connectionObservationFilter = new ConnectionObservationFilter();
        dataSource.setProxyFilters(Arrays.asList(connectionObservationFilter, logFilter, statFilter, wallFilter));

        dataSource.init();
        return dataSource;

//		AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
//		atomikosDataSourceBean.setXaDataSource(dataSource);
//		atomikosDataSourceBean.setBeanName(properties.getName());
//		atomikosDataSourceBean.afterPropertiesSet();
//		return atomikosDataSourceBean;
    }

    public static DynamicRoutingDataSource createDynamicRoutingDataSource(String defaultClusterName,
                                                                          List<DataSourceProperties> dataSourceProperties) {
        Map<String, Map<String, List<String>>> dataSourceIndexMap = new HashMap<>();
        Map<String, DataSource> dataSourceMap = batchCreate(dataSourceProperties, dataSourceIndexMap);
        DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource(defaultClusterName,
                dataSourceMap, dataSourceIndexMap);
        return dynamicRoutingDataSource;
    }
}
