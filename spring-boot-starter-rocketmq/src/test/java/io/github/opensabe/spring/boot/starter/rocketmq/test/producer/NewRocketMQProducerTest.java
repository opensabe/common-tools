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
package io.github.opensabe.spring.boot.starter.rocketmq.test.producer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import io.github.opensabe.spring.boot.starter.rocketmq.MQProducerImpl;
import io.github.opensabe.spring.boot.starter.rocketmq.test.common.BaseRocketMQTest;

public class NewRocketMQProducerTest extends BaseRocketMQTest {
    @Test
    @DisplayName("验证MQProducer Bean 注入")
    public void testMQProducerBeanInject() {
        Assertions.assertNotNull(mqProducer);
        Assertions.assertInstanceOf(MQProducerImpl.class, mqProducer);
    }

    @DynamicPropertySource
    public static void setExtra(DynamicPropertyRegistry registry) {
        registry.add("rocketmq.extend.use-new-producer", () -> true);
    }
}
