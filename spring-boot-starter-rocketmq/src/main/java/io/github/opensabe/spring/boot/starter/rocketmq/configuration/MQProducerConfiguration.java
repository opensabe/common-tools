package io.github.opensabe.spring.boot.starter.rocketmq.configuration;

import io.github.opensabe.common.config.dal.db.dao.MqFailLogEntityMapper;
import io.github.opensabe.common.idgenerator.service.UniqueID;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.spring.boot.starter.rocketmq.MQLocalTransactionListener;
import io.github.opensabe.spring.boot.starter.rocketmq.MQProducer;
import io.github.opensabe.spring.boot.starter.rocketmq.MQProducerImpl;
import io.github.opensabe.spring.boot.starter.rocketmq.UniqueRocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class MQProducerConfiguration {
    @Bean
    @Primary
    public MQProducer getMQProducer(
            @Value("${spring.application.name:unknown}")
                    String srcName,
            UnifiedObservationFactory unifiedObservationFactory,
            RocketMQTemplate rocketMQTemplate,
            MqFailLogEntityMapper mqFailLogEntityMapper,
            UniqueID uniqueID,
            GlobalSecretManager globalSecretManager) {
        MQProducerImpl mqProducer = new MQProducerImpl(srcName, unifiedObservationFactory, rocketMQTemplate, mqFailLogEntityMapper, uniqueID, globalSecretManager);
        return mqProducer;
    }

    @Bean
    @Primary
    public MQLocalTransactionListener getMQLocalTransactionListener(List<UniqueRocketMQLocalTransactionListener> uniqueRocketMQLocalTransactionListeners) {
        return new MQLocalTransactionListener(uniqueRocketMQLocalTransactionListeners);
    }
}
