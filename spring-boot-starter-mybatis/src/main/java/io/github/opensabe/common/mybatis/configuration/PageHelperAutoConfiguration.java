package io.github.opensabe.common.mybatis.configuration;

import io.github.opensabe.common.mybatis.interceptor.DataSourceSwitchInterceptor;
import jakarta.annotation.PostConstruct;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Properties;

import static io.github.opensabe.common.mybatis.configuration.PageHelperProperties.PAGEHELPER_PREFIX;

/**
 * 配置分页插件
 *
 * @autor maheng
 */
@Configuration(proxyBeanMethods = false)
public class PageHelperAutoConfiguration {
    @Autowired
    private List<SqlSessionFactory> sqlSessionFactories;

    @Autowired
    private PageHelperProperties properties;

    @Autowired(required = false)
    private DataSourceSwitchInterceptor dataSourceSwitchInterceptor;
    /**
     * 接受分页插件额外的属性
     *
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = PAGEHELPER_PREFIX)
    public Properties pageHelperProperties() {
        return new Properties();
    }

    @PostConstruct
    public void addPageInterceptor() {
        if (dataSourceSwitchInterceptor != null) {
            Properties _properties = new Properties();
            _properties.putAll(pageHelperProperties());
            _properties.putAll(properties.getProperties());
            dataSourceSwitchInterceptor.setProperties(_properties);
            for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
                if (!containsInterceptor(sqlSessionFactory.getConfiguration(), dataSourceSwitchInterceptor)) {
                    sqlSessionFactory.getConfiguration().addInterceptor(dataSourceSwitchInterceptor);
                }
            }
        }
    }

    private boolean containsInterceptor(org.apache.ibatis.session.Configuration configuration, Interceptor interceptor) {
        try {
            return configuration.getInterceptors().contains(interceptor);
        } catch (Throwable var4) {
            return false;
        }
    }
}
