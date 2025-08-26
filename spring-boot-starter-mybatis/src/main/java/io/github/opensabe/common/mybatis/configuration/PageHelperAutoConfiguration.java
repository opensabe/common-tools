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

import java.util.List;
import java.util.Properties;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.common.mybatis.interceptor.DataSourceSwitchInterceptor;
import jakarta.annotation.PostConstruct;

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
            Properties pageHelperProps = new Properties();
            pageHelperProps.putAll(pageHelperProperties());
            pageHelperProps.putAll(properties.getProperties());
            dataSourceSwitchInterceptor.setProperties(pageHelperProps);
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
