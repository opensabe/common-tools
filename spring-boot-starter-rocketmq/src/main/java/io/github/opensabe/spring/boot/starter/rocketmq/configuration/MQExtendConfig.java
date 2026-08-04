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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.spring.boot.starter.rocketmq.RocketMQTemplateBeanPostProcessor;
import io.github.opensabe.spring.boot.starter.rocketmq.jfr.MessageConsumeToJFRGenerator;
import io.github.opensabe.spring.boot.starter.rocketmq.jfr.MessageProduceToJFRGenerator;

/**
 * RocketMQ 扩展配置 Bean。
 */
@Configuration
public class MQExtendConfig {
    /** rocketMQTemplateBeanPostProcessor。 */
    @Bean
    public RocketMQTemplateBeanPostProcessor rocketMQTemplateBeanPostProcessor() {
        return new RocketMQTemplateBeanPostProcessor();
    }

    /** messageConsumeToJFRGenerator。 */
    @Bean
    public MessageConsumeToJFRGenerator messageConsumeToJFRGenerator() {
        return new MessageConsumeToJFRGenerator();
    }

    /** messageProduceToJFRGenerator。 */
    @Bean
    public MessageProduceToJFRGenerator messageProduceToJFRGenerator() {
        return new MessageProduceToJFRGenerator();
    }
}
