package io.github.opensabe.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.Log4jAppenderCheckSecretCheckFilter;
import io.github.opensabe.common.utils.SpringUtil;
import io.github.opensabe.common.utils.json.JsonUtil;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SpringCommonUtilConfiguration {
    @Bean
    public SpringUtil getSpringUtil() {
        return new SpringUtil();
    }

    @Bean
    public GlobalSecretManager globalSecretManager() {
        return new GlobalSecretManager();
    }

    @Bean
    public Log4jAppenderCheckSecretCheckFilter log4jAppenderCheckSecretCheckFilter() {
        return new Log4jAppenderCheckSecretCheckFilter();
    }

    @Bean
    @ConditionalOnBean(ObjectMapper.class)
    public CommandLineRunner initializeJsonUtil(ObjectMapper objectMapper) {
        return args -> {
            JsonUtil.setGlobalObjectMapper(objectMapper);
        };
    }
}
