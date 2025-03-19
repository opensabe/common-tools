package io.github.opensabe.common.mybatis.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
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

				if (Objects.nonNull(configuration.getMapUnderscoreToCamelCase())) {
					o.setMapUnderscoreToCamelCase(configuration.getMapUnderscoreToCamelCase());
				}

				if (Objects.nonNull(configuration.getAggressiveLazyLoading())) {
					o.setAggressiveLazyLoading(configuration.getAggressiveLazyLoading());
				}

				o.setAutoMappingBehavior(configuration.getAutoMappingBehavior());
				o.setAutoMappingUnknownColumnBehavior(configuration.getAutoMappingUnknownColumnBehavior());

				if (Objects.nonNull(configuration.getCacheEnabled())) {
					o.setCacheEnabled(configuration.getCacheEnabled());
				}

				if (Objects.nonNull(configuration.getCallSettersOnNulls())) {
					o.setCallSettersOnNulls(configuration.getCallSettersOnNulls());
				}

				if (Objects.nonNull(configuration.getLazyLoadingEnabled())) {
					o.setLazyLoadingEnabled(configuration.getLazyLoadingEnabled());
				}
			});
		}
	}
}
