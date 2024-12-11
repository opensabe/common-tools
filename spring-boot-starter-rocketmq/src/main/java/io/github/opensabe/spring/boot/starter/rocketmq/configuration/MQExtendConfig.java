package io.github.opensabe.spring.boot.starter.rocketmq.configuration;

import io.github.opensabe.spring.boot.starter.rocketmq.RocketMQTemplateBeanPostProcessor;
import io.github.opensabe.spring.boot.starter.rocketmq.jfr.MessageConsumeToJFRGenerator;
import io.github.opensabe.spring.boot.starter.rocketmq.jfr.MessageProduceToJFRGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQExtendConfig {
    @Bean
    public RocketMQTemplateBeanPostProcessor rocketMQTemplateBeanPostProcessor() {
        return new RocketMQTemplateBeanPostProcessor();
    }

    @Bean
    public MessageConsumeToJFRGenerator messageConsumeToJFRGenerator() {
        return new MessageConsumeToJFRGenerator();
    }

    @Bean
    public MessageProduceToJFRGenerator messageProduceToJFRGenerator() {
        return new MessageProduceToJFRGenerator();
    }
}
