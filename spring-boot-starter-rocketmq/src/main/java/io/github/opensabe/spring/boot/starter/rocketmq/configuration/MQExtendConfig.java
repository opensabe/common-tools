package io.github.opensabe.spring.boot.starter.rocketmq.configuration;

import io.github.opensabe.spring.boot.starter.rocketmq.RocketMQTemplateBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQExtendConfig {
    @Bean
    public RocketMQTemplateBeanPostProcessor rocketMQTemplateBeanPostProcessor() {
        return new RocketMQTemplateBeanPostProcessor();
    }
}
