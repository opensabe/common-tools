package io.github.opensabe.youtobe.config;

import io.github.opensabe.youtobe.properties.YouToBeDataApiProperties;
import io.github.opensabe.youtobe.service.YouToBeSearchService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * YouToBeSearchService自动装配
 */
@Configuration
@EnableConfigurationProperties(YouToBeDataApiProperties.class)
public class YouToBeSearchAutoConfig {

    @Autowired
    private OkHttpClient okHttpClient;

    @Bean
    @ConditionalOnMissingBean
    public YouToBeSearchService youToBeSearchService() {
        return new YouToBeSearchService(okHttpClient);
    }
}
