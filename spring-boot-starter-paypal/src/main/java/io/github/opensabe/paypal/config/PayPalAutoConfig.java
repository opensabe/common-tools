package io.github.opensabe.paypal.config;

import io.github.opensabe.paypal.service.PayPalService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PayPal 自动装配类
 */
@Configuration
@EnableConfigurationProperties(PayPalProperties.class)
public class PayPalAutoConfig {

    @Autowired
    private OkHttpClient okHttpClient;

    @Bean
    @ConditionalOnMissingBean
    public PayPalService payPalService() {
        return new PayPalService(okHttpClient);
    }
}
