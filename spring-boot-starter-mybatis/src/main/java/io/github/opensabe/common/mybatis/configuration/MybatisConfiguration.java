package io.github.opensabe.common.mybatis.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import tk.mybatis.mapper.autoconfigure.MybatisProperties;

import java.util.List;

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

//				o.setMapUnderscoreToCamelCase(configuration.isMapUnderscoreToCamelCase());
				o.setMapUnderscoreToCamelCase(configuration.getMapUnderscoreToCamelCase());

//				o.setAggressiveLazyLoading(configuration.isAggressiveLazyLoading());
				o.setAggressiveLazyLoading(configuration.getAggressiveLazyLoading());

				o.setAutoMappingBehavior(configuration.getAutoMappingBehavior());
				o.setAutoMappingUnknownColumnBehavior(configuration.getAutoMappingUnknownColumnBehavior());

//				o.setCacheEnabled(configuration.isCacheEnabled());
				o.setCacheEnabled(configuration.getCacheEnabled());

//				o.setAggressiveLazyLoading(configuration.isAggressiveLazyLoading());
				o.setAggressiveLazyLoading(configuration.getAggressiveLazyLoading());

//				o.setCallSettersOnNulls(configuration.isCallSettersOnNulls());
				o.setCallSettersOnNulls(configuration.getCallSettersOnNulls());

//				o.setLazyLoadingEnabled(configuration.isLazyLoadingEnabled());
				o.setLazyLoadingEnabled(configuration.getLazyLoadingEnabled());
			});
		}
	}
}
