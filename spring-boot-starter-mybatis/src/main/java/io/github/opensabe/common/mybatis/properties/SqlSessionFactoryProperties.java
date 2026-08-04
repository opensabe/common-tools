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
package io.github.opensabe.common.mybatis.properties;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SqlSessionFactory 配置属性。
 */
@Configuration
@EnableConfigurationProperties(SqlSessionFactoryProperties.class)
@ConfigurationProperties(prefix = SqlSessionFactoryProperties.PREFIX)
public class SqlSessionFactoryProperties {

    public static final String PREFIX = "jdbc";
/** config。 */
    private Map<String, DatasourceConfiguration> config;

    /**
     * @return config
     */
    public Map<String, DatasourceConfiguration> getConfig() {
        return config;
    }

    /**
     * @param config 待设置值
     */
    public void setConfig(Map<String, DatasourceConfiguration> config) {
        this.config = config;
    }

    public static class DatasourceConfiguration {
/** defaultClusterName。 */
        private String defaultClusterName;
/** basePackages。 */
        private String[] basePackages;
/** transactionServicePackages。 */
        private String[] transactionServicePackages;
/** dataSource 配置属性。 */
        private List<DataSourceProperties> dataSourceProperties;

        /**
         * @return basePackages
         */
        public String[] getBasePackages() {
            return basePackages;
        }

        /**
         * @param basePackages 待设置值
         */
        public void setBasePackages(String[] basePackages) {
            this.basePackages = basePackages;
        }

        /**
         * @return dataSource
         */
        public List<DataSourceProperties> getDataSource() {
            return dataSourceProperties;
        }

        /**
         * @param dataSource 待设置值
         */
        public void setDataSource(List<DataSourceProperties> dataSource) {
            this.dataSourceProperties = dataSource;
        }

        /**
         * @return defaultClusterName
         */
        public String getDefaultClusterName() {
            return defaultClusterName;
        }

        /**
         * @param defaultClusterName 待设置值
         */
        public void setDefaultClusterName(String defaultClusterName) {
            this.defaultClusterName = defaultClusterName;
        }

        /**
         * @return transactionServicePackages
         */
        public String[] getTransactionServicePackages() {
            return transactionServicePackages;
        }

        /**
         * @param transactionServicePackages 待设置值
         */
        public void setTransactionServicePackages(String[] transactionServicePackages) {
            this.transactionServicePackages = transactionServicePackages;
        }

        /** {@inheritDoc} — 返回字符串表示。 */
        @Override
        public String toString() {
            return "DatasourceConfiguration [defaultClusterName=" + defaultClusterName + ", basePackages="
                    + Arrays.toString(basePackages) + ", transactionServicePackages="
                    + Arrays.toString(transactionServicePackages) + ", dataSourceProperties=" + dataSourceProperties
                    + "]";
        }
    }
}
