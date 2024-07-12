package io.github.opensabe.common.mybatis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(SqlSessionFactoryProperties.class)
@ConfigurationProperties(prefix = SqlSessionFactoryProperties.PREFIX)
public class SqlSessionFactoryProperties {

	public static class DatasourceConfiguration {
		private String defaultClusterName;
		private String[] basePackages;
		private String[] transactionServicePackages;
		private List<DataSourceProperties> dataSourceProperties;

		public String[] getBasePackages() {
			return basePackages;
		}

		public List<DataSourceProperties> getDataSource() {
			return dataSourceProperties;
		}

		public String getDefaultClusterName() {
			return defaultClusterName;
		}

		public String[] getTransactionServicePackages() {
			return transactionServicePackages;
		}

		public void setBasePackages(String[] basePackages) {
			this.basePackages = basePackages;
		}

		public void setDataSource(List<DataSourceProperties> dataSource) {
			this.dataSourceProperties = dataSource;
		}

		public void setDefaultClusterName(String defaultClusterName) {
			this.defaultClusterName = defaultClusterName;
		}

		public void setTransactionServicePackages(String[] transactionServicePackages) {
			this.transactionServicePackages = transactionServicePackages;
		}

		@Override
		public String toString() {
			return "DatasourceConfiguration [defaultClusterName=" + defaultClusterName + ", basePackages="
					+ Arrays.toString(basePackages) + ", transactionServicePackages="
					+ Arrays.toString(transactionServicePackages) + ", dataSourceProperties=" + dataSourceProperties
					+ "]";
		}
	}

	public static final String PREFIX = "jdbc";

	private Map<String, DatasourceConfiguration> config;

	public Map<String, DatasourceConfiguration> getConfig() {
		return config;
	}

	public void setConfig(Map<String, DatasourceConfiguration> config) {
		this.config = config;
	}
}
