package io.github.opensabe.spring.boot.starter.rocketmq.configuration;

import io.github.opensabe.common.config.dal.db.dao.MqFailLogEntityMapper;
import io.github.opensabe.common.idgenerator.service.UniqueID;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.spring.boot.starter.rocketmq.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class MQProducerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(SqlSessionFactory.class)
    public MessagePersistent mybatisMessagePersistent (MqFailLogEntityMapper mapper) {
        return mapper::insertSelective;
    }

    @Bean
    @Primary
    public MQProducer getMQProducer(
            @Value("${spring.application.name:unknown}")
                    String srcName,
            UnifiedObservationFactory unifiedObservationFactory,
            RocketMQTemplate rocketMQTemplate,
            MessagePersistent persistent,
            UniqueID uniqueID,
            GlobalSecretManager globalSecretManager) {
        MQProducerImpl mqProducer = new MQProducerImpl(srcName, unifiedObservationFactory, rocketMQTemplate, persistent, uniqueID, globalSecretManager);
        return mqProducer;
    }

    @Bean
    @Primary
    public MQLocalTransactionListener getMQLocalTransactionListener(List<UniqueRocketMQLocalTransactionListener> uniqueRocketMQLocalTransactionListeners) {
        return new MQLocalTransactionListener(uniqueRocketMQLocalTransactionListeners);
    }
}
