package io.github.opensabe.springdoc.config;

import io.github.opensabe.base.vo.IntValueEnum;
import io.github.opensabe.springdoc.converters.EnumModelConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author heng.ma
 */
@ConditionalOnClass(IntValueEnum.class)
@Configuration(proxyBeanMethods = false)
public class FrameworkConfig {

    @Bean
    @ConditionalOnMissingBean
    public EnumModelConverter enumModelConverter () {
        return new EnumModelConverter();
    }
}
