package io.github.opensabe.common.dynamodb.config;


import io.github.opensabe.common.dynamodb.jfr.DynamodbExecuteJFRGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MonitorConfiguration {

    /**
     * SQL执行时间监控上报JFR事件
     * @return
     */
    @Bean
    public DynamodbExecuteJFRGenerator dynamodbExecuteJFRGenerator () {
        return new DynamodbExecuteJFRGenerator();
    }
}
