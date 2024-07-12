package io.github.opensabe.spring.boot.starter.rocketmq.autoconf;

import io.github.opensabe.spring.boot.starter.rocketmq.configuration.MQExtendConfig;
import io.github.opensabe.spring.boot.starter.rocketmq.configuration.MQProducerConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({MQProducerConfiguration.class, MQExtendConfig.class})
public class RocketMQAutoConfiguration {
}