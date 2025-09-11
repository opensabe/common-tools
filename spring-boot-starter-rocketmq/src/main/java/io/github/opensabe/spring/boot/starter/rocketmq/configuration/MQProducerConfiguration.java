/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.spring.boot.starter.rocketmq.configuration;

import java.util.List;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.common.config.dal.db.dao.MqFailLogEntityMapper;
import io.github.opensabe.common.idgenerator.service.UniqueID;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.spring.boot.starter.rocketmq.MQLocalTransactionListener;
import io.github.opensabe.spring.boot.starter.rocketmq.MQProducer;
import io.github.opensabe.spring.boot.starter.rocketmq.MQProducerImpl;
import io.github.opensabe.spring.boot.starter.rocketmq.MessagePersistent;
import io.github.opensabe.spring.boot.starter.rocketmq.OldDefaultMQProducerImpl;
import io.github.opensabe.spring.boot.starter.rocketmq.RocketMQListenerContainerBeanPostProcessor;
import io.github.opensabe.spring.boot.starter.rocketmq.UniqueRocketMQLocalTransactionListener;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RocketMQExtendProperties.class)
public class MQProducerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(SqlSessionFactory.class)
    public MessagePersistent mybatisMessagePersistent(MqFailLogEntityMapper mapper) {
        return mapper::insertSelective;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBooleanProperty(
            prefix = "rocketmq.extend",
            name = "use-new-producer",
            havingValue = true,
            matchIfMissing = false
    )
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

    /**
     * 旧版生产者, 兼容以前的版本，默认启用
     *
     * @param srcName
     * @param unifiedObservationFactory
     * @param rocketMQTemplate
     * @param persistent
     * @param uniqueID
     * @param globalSecretManager
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBooleanProperty(
            prefix = "rocketmq.extend",
            name = "use-new-producer",
            havingValue = false,
            matchIfMissing = true
    )
    public MQProducer getOldDefaultMQProducer(
            @Value("${spring.application.name:unknown}")
            String srcName,
            UnifiedObservationFactory unifiedObservationFactory,
            RocketMQTemplate rocketMQTemplate,
            @Autowired(required = false) MessagePersistent persistent,
            @Autowired(required = false) UniqueID uniqueID,
            GlobalSecretManager globalSecretManager) {
        OldDefaultMQProducerImpl mqProducer = new OldDefaultMQProducerImpl(srcName, unifiedObservationFactory, rocketMQTemplate, persistent, uniqueID, globalSecretManager);
        return mqProducer;
    }

    @Bean
    @ConditionalOnMissingBean
    public MQLocalTransactionListener getMQLocalTransactionListener(List<UniqueRocketMQLocalTransactionListener> uniqueRocketMQLocalTransactionListeners) {
        return new MQLocalTransactionListener(uniqueRocketMQLocalTransactionListeners);
    }

    @Bean
    @ConditionalOnMissingBean
    public RocketMQListenerContainerBeanPostProcessor rocketMQListenerContainerBeanPostProcessor() {
        return new RocketMQListenerContainerBeanPostProcessor();
    }
}
