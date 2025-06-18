package io.github.opensabe.springdoc.config;

import io.github.opensabe.springdoc.converters.DateTimeModelConverter;
import io.github.opensabe.springdoc.converters.VoidModelResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author heng.ma
 */
@Configuration(proxyBeanMethods = false)
public class GenerateConfig {

    @Bean
    @ConditionalOnMissingBean
    public DateTimeModelConverter dateTimeModelConverter () {
        return new DateTimeModelConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public VoidModelResolver voidModelResolver () {
        return new VoidModelResolver();
    }

}
