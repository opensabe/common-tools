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

import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.github.opensabe.common.mybatis.interceptor.CustomizedDataSourceTransactionManager;
import io.github.opensabe.common.mybatis.interceptor.CustomizedTransactionInterceptor;
import io.github.opensabe.common.mybatis.plugins.BeanDefiner;
import io.github.opensabe.common.mybatis.plugins.DataSourceFactory;
import io.github.opensabe.common.mybatis.plugins.DynamicRoutingDataSource;
import io.github.opensabe.common.mybatis.properties.CountryProperties;
import io.github.opensabe.common.mybatis.properties.SqlSessionFactoryProperties;
import lombok.extern.log4j.Log4j2;
import tk.mybatis.mapper.autoconfigure.MybatisProperties;
import tk.mybatis.mapper.autoconfigure.SpringBootVFS;
import tk.mybatis.spring.mapper.ClassPathMapperScanner;
import tk.mybatis.spring.mapper.SpringBootBindUtil;

/**
 * 配置sqlSessionFactory
 * <p>
 * 根据业务划分不同的sqlSessionFactory, 不同的sqlSessionFactory扫描不同的mapper,
 * 每个sqlSessionFactory由多个dataSource
 * </p>
 *
 * @author maheng
 */
@Log4j2
@EnableConfigurationProperties(SqlSessionFactoryProperties.class)
@Configuration(proxyBeanMethods = false)
public class SqlSessionFactoryConfiguration
        implements ResourceLoaderAware, EnvironmentAware, BeanFactoryPostProcessor {

    public static CountryProperties countryProperties;
    public static String defaultOperId;
    private Environment environment;
    private ResourceLoader resourceLoader;

    /**
     * 使用指定的sqlSessionFactory扫描不同的mapper
     *
     * @param registry         applicationContext
     * @param sqlStringFactory sqlSessionFactory
     * @param basePackage      mapper路径
     */
    private void doScan(BeanDefinitionRegistry registry, String sqlStringFactory, String... basePackage) {
        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
        scanner.setMapperProperties(environment);
        scanner.setSqlSessionFactoryBeanName(sqlStringFactory);
        scanner.setResourceLoader(resourceLoader);
        scanner.registerFilters();
        scanner.doScan(basePackage);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            SqlSessionFactoryProperties sqlSessionFactoryProperties = SpringBootBindUtil.bind(environment,
                    SqlSessionFactoryProperties.class, SqlSessionFactoryProperties.PREFIX);
            Map<String, SqlSessionFactoryProperties.DatasourceConfiguration> configuration = sqlSessionFactoryProperties
                    .getConfig();
            if (configuration != null) {
                MybatisProperties mybatisProperties = SpringBootBindUtil.bind(environment, MybatisProperties.class,
                        MybatisProperties.MYBATIS_PREFIX);
                DefaultListableBeanFactory register = (DefaultListableBeanFactory) beanFactory;
                BeanDefiner beanDefiner = new BeanDefiner(register);
                configuration.forEach((k, v) -> {
                    // log.info("key:" + k);
                    // log.info("value:" + v.toString());

                    Assert.notEmpty(v.getDataSource(), "datasource shall not be null");
                    Assert.notEmpty(v.getBasePackages(), "basePackages shall not be null");
                    Assert.hasLength(v.getDefaultClusterName(), "clusterName shall not be null");

                    // Create Datasource
                    DynamicRoutingDataSource dynamicRoutingDataSource = DataSourceFactory
                            .createDynamicRoutingDataSource(v.getDefaultClusterName(), v.getDataSource());
                    beanDefiner.registerDataSource(k, dynamicRoutingDataSource);

                    // Create and Register Transaction Manager
                    String transactionManagerBeanName = beanDefiner.registerTransactionManager(k,
                            transactionManager(dynamicRoutingDataSource));
                    if (v.getTransactionServicePackages() != null) {
                        for (String packageName : v.getTransactionServicePackages()) {
                            log.info("register transaction manager name:{},{}", packageName, transactionManagerBeanName);
                            CustomizedTransactionInterceptor.putTransactionManagerName(packageName,
                                    transactionManagerBeanName);
                        }
                    }

                    // Create and Register SqlSessionFactory and Add Datasource Switch Interceptor
                    SqlSessionFactory sqlSessionFactory = sqlSessionFactory(k, dynamicRoutingDataSource, mybatisProperties);

                    String factory = beanDefiner.registerSqlSessionFactory(k, sqlSessionFactory);
                    beanDefiner.registerSqlSessionTemplate(k, sqlSessionTemplate(sqlSessionFactory));

                    // Scan Mapper
                    doScan(register, factory, v.getBasePackages());
                });

                /**
                 * 手动实现事务管理代理
                 * @see org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration
                 */
                CustomizedTransactionInterceptor interceptor = new CustomizedTransactionInterceptor();
                try {
                    register.removeBeanDefinition("transactionInterceptor");
                } catch (NoSuchBeanDefinitionException e) {
                    log.info("No transactionInterceptor found, no need to remove");
                }
                try {
                    register.removeBeanDefinition("transactionAttributeSource");
                } catch (NoSuchBeanDefinitionException e) {
                    log.info("No transactionAttributeSource found, no need to remove");
                }
                try {
                    register.removeBeanDefinition("beanFactoryTransactionAttributeSourceAdvisor");
                } catch (NoSuchBeanDefinitionException e) {
                    log.info("No beanFactoryTransactionAttributeSourceAdvisor found, no need to remove");
                }
                register.registerBeanDefinition("transactionInterceptor",
                        BeanDefinitionBuilder
                                .genericBeanDefinition(CustomizedTransactionInterceptor.class, () -> interceptor)
                                .setRole(BeanDefinition.ROLE_INFRASTRUCTURE).getBeanDefinition());

                TransactionAttributeSource transactionAttributeSource = new AnnotationTransactionAttributeSource();
                register.registerBeanDefinition("transactionAttributeSource",
                        BeanDefinitionBuilder
                                .genericBeanDefinition(TransactionAttributeSource.class, () -> transactionAttributeSource)
                                .setRole(BeanDefinition.ROLE_INFRASTRUCTURE).getBeanDefinition());
                interceptor.setTransactionAttributeSource(transactionAttributeSource);

                BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
                advisor.setTransactionAttributeSource(transactionAttributeSource);
                advisor.setAdvice(interceptor);
                register.registerBeanDefinition("beanFactoryTransactionAttributeSourceAdvisor",
                        BeanDefinitionBuilder
                                .genericBeanDefinition(BeanFactoryTransactionAttributeSourceAdvisor.class, () -> advisor)
                                .setRole(BeanDefinition.ROLE_INFRASTRUCTURE).getBeanDefinition());
            }
        } catch (BeansException e) {
            log.error("catch Exception {} while initiate sqlSessionFactory", e.getLocalizedMessage(), e);
            throw e;
        } catch (Throwable e) {
            log.error("catch Exception {} while initiate sqlSessionFactory", e.getLocalizedMessage(), e);
            throw new BeanCreationException("initiate error " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        SqlSessionFactoryConfiguration.countryProperties = SpringBootBindUtil.bind(environment, CountryProperties.class, CountryProperties.PREFIX);
        SqlSessionFactoryConfiguration.defaultOperId = environment.getProperty("defaultOperId");
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 创建sqlSessionFactory 初始化时不能设置configuration
     *
     * @param k          业务类型
     * @param dataSource 数据源
     * @param properties mybatis配置
     * @return sqlSessionFactory
     */
    private SqlSessionFactory sqlSessionFactory(String k, DataSource dataSource, MybatisProperties properties) {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        //为了添加Observation,我们自己指定configuration,而设置了configuration就不能设置configLocation
        //自定义的Configuration重写了MapperRegistry,注册Mapper时生成我们自己的代理类，在代理类上添加Observation
        factory.setConfiguration(new io.github.opensabe.common.mybatis.plugins.Configuration());
//        if (StringUtils.hasText(properties.getConfigLocation())) {
//            factory.setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
//        }
        Properties configurationProperties = properties.getConfigurationProperties();
        if (configurationProperties != null) {
            factory.setConfigurationProperties(configurationProperties);
        }
        String typeAliasesPackage = properties.getTypeAliasesPackage();
        if (StringUtils.hasLength(typeAliasesPackage)) {
            factory.setTypeAliasesPackage(typeAliasesPackage);
        }
        if (properties.getTypeAliasesSuperType() != null) {
            factory.setTypeAliasesSuperType(properties.getTypeAliasesSuperType());
        }
        if (StringUtils.hasLength(properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(properties.getTypeHandlersPackage());
        }
        if (!ObjectUtils.isEmpty(properties.resolveMapperLocations())) {
            factory.setMapperLocations(properties.resolveMapperLocations());
        }
        try {
            return factory.getObject();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
        return sqlSessionTemplate;
    }

    private DataSourceTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new CustomizedDataSourceTransactionManager(dataSource);
        return transactionManager;
    }
}
