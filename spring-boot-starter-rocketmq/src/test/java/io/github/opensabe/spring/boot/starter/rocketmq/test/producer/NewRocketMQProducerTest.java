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

/**
 * 新版 RocketMQ Producer（{@code use-new-producer=true}）Bean 注入测试。
 */
@DisplayName("新版 RocketMQ Producer 测试")
public class NewRocketMQProducerTest extends BaseRocketMQTest {
    /**
     * 验证 MQProducer 注入且实现类为 {@link MQProducerImpl}。
     */
    @DisplayName("验证mQProducerBeanInject")
    @Test
    public void testMQProducerBeanInject() {
        Assertions.assertNotNull(mqProducer);
        Assertions.assertInstanceOf(MQProducerImpl.class, mqProducer);
    }

    /**
     * @param extra 待设置值
     */
    @DynamicPropertySource
    public static void setExtra(DynamicPropertyRegistry registry) {
        registry.add("rocketmq.extend.use-new-producer", () -> true);
    }
}
