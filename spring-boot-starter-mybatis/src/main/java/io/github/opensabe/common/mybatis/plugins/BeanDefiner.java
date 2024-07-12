package io.github.opensabe.common.mybatis.plugins;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

import static java.lang.String.format;

/**
 * 动态创建spring对象
 * @author maheng
 */
public class BeanDefiner {

    private DefaultListableBeanFactory register;

    public BeanDefiner(DefaultListableBeanFactory register) {
        this.register = register;
    }

    /**
     * dataSource构造器
     * @param dataSource
     * @return
     */
    public BeanDefinition dataSource(DataSource dataSource){
        return BeanDefinitionBuilder.genericBeanDefinition(DataSource.class,() -> dataSource)
                .getBeanDefinition();
    }
    public BeanDefinition sqlSessionFactory(SqlSessionFactory sqlSessionFactory){
        return BeanDefinitionBuilder.genericBeanDefinition(SqlSessionFactory.class,() -> sqlSessionFactory)
                .getBeanDefinition();
    }
    public BeanDefinition sqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate){
        return BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplate.class,() -> sqlSessionTemplate)
                .getBeanDefinition();
    }
    public BeanDefinition transactionManager(DataSourceTransactionManager transactionManager){
        return BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class,() -> transactionManager)
                .getBeanDefinition();
    }

    /**
     * 注册dataSource
     * @param key       sqlSessionFactory类型
     * @param dataSource  dataSource
     * @return beanName
     */
    public String registerDataSource(String key,DataSource dataSource){
        String beanName = format("%s.dataSource", key);
        register.registerBeanDefinition(beanName,dataSource(dataSource));
        return beanName;
    }
    /**
     * 注册sqlSessionFactory
     * @param key       sqlSessionFactory类型
     * @param sqlSessionFactory  dataSource
     * @return beanName
     */
    public String registerSqlSessionFactory(String key,SqlSessionFactory sqlSessionFactory){
        String beanName = format("%s.sqlSessionFactory", key);
        register.registerBeanDefinition(beanName,sqlSessionFactory(sqlSessionFactory));
        return beanName;
    }
    public String registerSqlSessionTemplate(String key,SqlSessionTemplate sqlSessionTemplate){
        String beanName = format("%s.sqlSessionTemplate", key);
        register.registerBeanDefinition(beanName,sqlSessionTemplate(sqlSessionTemplate));
        return beanName;
    }
    public String registerTransactionManager(String key,DataSourceTransactionManager transactionManager){
        String beanName = format("%s.transactionManager", key);
        register.registerBeanDefinition(beanName,transactionManager(transactionManager));
        return beanName;
    }
}
