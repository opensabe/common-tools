package io.github.opensabe.youtobe.config;

import io.github.opensabe.youtobe.properties.YouToBeDataApiProperties;
import io.github.opensabe.youtobe.service.YouToBeSearchService;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * YouToBeSearchService自动装配
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(YouToBeDataApiProperties.class)
public class YouToBeSearchAutoConfig {


    @Bean
    @ConditionalOnMissingBean
    public YouToBeSearchService youToBeSearchService(OkHttpClient okHttpClient, YouToBeDataApiProperties properties) {
        return new YouToBeSearchService(properties, okHttpClient);
    }
}
