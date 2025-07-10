package io.github.opensabe.spring.boot.starter.rocketmq.configuration;

import io.github.opensabe.common.config.dal.db.dao.MqFailLogEntityMapper;
import io.github.opensabe.common.idgenerator.service.UniqueID;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.spring.boot.starter.rocketmq.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @ConditionalOnMissingBean
    public MQProducer getMQProducer(
            @Value("${spring.application.name:unknown}")
                    String srcName,
            UnifiedObservationFactory unifiedObservationFactory,
            RocketMQTemplate rocketMQTemplate,
            @Autowired(required = false) MessagePersistent persistent,
            @Autowired(required = false) UniqueID uniqueID,
            GlobalSecretManager globalSecretManager) {
        MQProducerImpl mqProducer = new MQProducerImpl(srcName, unifiedObservationFactory, rocketMQTemplate, persistent, uniqueID, globalSecretManager);
        return mqProducer;
    }

    @Bean
    @ConditionalOnMissingBean
    public MQLocalTransactionListener getMQLocalTransactionListener(List<UniqueRocketMQLocalTransactionListener> uniqueRocketMQLocalTransactionListeners) {
        return new MQLocalTransactionListener(uniqueRocketMQLocalTransactionListeners);
    }

    @Bean
    @ConditionalOnMissingBean
    public RocketMQListenerContainerBeanPostProcessor rocketMQListenerContainerBeanPostProcessor () {
        return new RocketMQListenerContainerBeanPostProcessor();
    }
}
