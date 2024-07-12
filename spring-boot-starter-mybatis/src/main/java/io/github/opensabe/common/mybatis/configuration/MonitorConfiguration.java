package io.github.opensabe.common.mybatis.configuration;


import io.github.opensabe.common.mybatis.jfr.ConnectionJFRGenerator;
import io.github.opensabe.common.mybatis.jfr.SQLExecuteJFRGenerator;
import io.github.opensabe.common.mybatis.monitor.MonitorTransactionAspect;
import io.github.opensabe.common.mybatis.properties.SqlSessionFactoryProperties;
import io.github.opensabe.common.secret.GlobalSecretManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MonitorConfiguration {

    @Bean
    public MonitorTransactionAspect monitorTransactionAspect() {
        return new MonitorTransactionAspect();
    }

    /**
     * SQL执行时间监控上报JFR事件
     * @return
     */
    @Bean
    public SQLExecuteJFRGenerator sqlExecutorJFRGenerator () {
        return new SQLExecuteJFRGenerator();
    }

    @Bean
    public ConnectionJFRGenerator connectionJFRGenerator() {
        return new ConnectionJFRGenerator();
    }

    @Bean
    public DatabaseSecretProvider databaseSecretProvider(GlobalSecretManager globalSecretManager, SqlSessionFactoryProperties sqlSessionFactoryProperties) {
        return new DatabaseSecretProvider(globalSecretManager, sqlSessionFactoryProperties);
    }
}
