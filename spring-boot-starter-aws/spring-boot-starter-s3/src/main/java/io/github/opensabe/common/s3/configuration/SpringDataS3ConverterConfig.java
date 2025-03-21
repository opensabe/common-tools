package io.github.opensabe.common.s3.configuration;

import io.github.opensabe.common.s3.properties.S3Properties;
import io.github.opensabe.common.s3.service.FileService;
import io.github.opensabe.common.s3.typehandler.S3JsonConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.PersistentProperty;

/**
 * @author heng.ma
 */
@ConditionalOnClass(PersistentProperty.class)
@Configuration(proxyBeanMethods = false)
public class SpringDataS3ConverterConfig {

    @Bean
    @ConditionalOnMissingBean
    public S3JsonConverter s3JsonConverter (FileService fileService, S3Properties properties) {
        return new S3JsonConverter(fileService, properties);
    }

}
