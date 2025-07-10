package io.github.opensabe.springdoc.config;

import io.github.opensabe.spring.cloud.parent.common.handler.ErrorMessage;
import io.github.opensabe.springdoc.responses.SpringdocResponseService;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.service.OperationService;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author heng.ma
 */
@ConditionalOnClass({ErrorMessage.class, OperationService.class})
@Configuration(proxyBeanMethods = false)
public class CloudConfig {

    @Bean
    @ConditionalOnMissingBean
    public SpringdocResponseService springdocResponseService (OperationService operationService, SpringDocConfigProperties springDocConfigProperties, PropertyResolverUtils propertyResolverUtils) {
        return new SpringdocResponseService(operationService, springDocConfigProperties, propertyResolverUtils);
    }
}
