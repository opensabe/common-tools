package io.github.opensabe.paypal.config;

import io.github.opensabe.paypal.service.PayPalService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * PayPal 自动装配类
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PayPalProperties.class)
public class PayPalAutoConfig {


    @Bean
    @ConditionalOnMissingBean
    public PayPalService payPalService(PayPalProperties properties, StringRedisTemplate redisTemplate) {
        return new PayPalService(redisTemplate, properties);
    }
}
