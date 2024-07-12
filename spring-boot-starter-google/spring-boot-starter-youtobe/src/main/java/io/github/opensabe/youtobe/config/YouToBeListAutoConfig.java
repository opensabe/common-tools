package io.github.opensabe.youtobe.config;

import io.github.opensabe.youtobe.properties.YouToBeDataApiProperties;
import io.github.opensabe.youtobe.service.YouToBeListService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * YouToBeListService自动装配
 */
@Configuration
@EnableConfigurationProperties(YouToBeDataApiProperties.class)
public class YouToBeListAutoConfig {

    @Autowired
    private OkHttpClient okHttpClient;

    @Bean
    @ConditionalOnMissingBean
    public YouToBeListService youToBeListService() {
        return new YouToBeListService(okHttpClient);
    }
}
