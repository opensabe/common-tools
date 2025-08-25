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
package io.github.opensabe.common.alive.client.config;

import com.alibaba.fastjson.support.spring.messaging.MappingFastJsonMessageConverter;
import io.github.opensabe.common.alive.client.Client;
import io.github.opensabe.common.alive.client.MQClientImpl;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import static io.github.opensabe.common.alive.client.config.AliveProperties.ROCKET_CLIENT_NAME;


//@Log4j2
@Configuration(proxyBeanMethods = false)
public class AliveConfiguration {
    @Autowired
    private AliveProperties aliveProperties;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;
    @Primary
    @Bean(ROCKET_CLIENT_NAME)
    @ConditionalOnProperty(prefix = "alive.push",name = "rocketmq.name-server")
    public Client rocketAliveClient (Environment environment) {
         var producer = new RocketMQAutoConfiguration(environment).
                defaultMQProducer(aliveProperties.getRocketmq());
        producer.setInstanceName("aliveProducer");
        try {
            producer.start();
        } catch (MQClientException e) {
            throw new BeanDefinitionValidationException(String.format("Failed to startup MQProducer for RocketMQTemplate {}",ROCKET_CLIENT_NAME), e);
        }
        var template = new RocketMQTemplate();
        template.setProducer(producer);
        template.setMessageConverter(new MappingFastJsonMessageConverter());
        return new MQClientImpl(template,aliveProperties.getProduct(),unifiedObservationFactory);
    }

}
