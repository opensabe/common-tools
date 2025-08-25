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
