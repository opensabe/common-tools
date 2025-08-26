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
package io.github.opensabe.spring.boot.starter.rocketmq;

import java.util.concurrent.ExecutorService;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import jakarta.annotation.Nonnull;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RocketMQTemplateBeanPostProcessor implements BeanPostProcessor {
    @Autowired
    private ThreadPoolFactory threadPoolFactory;

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (bean instanceof RocketMQTemplate rocketMQTemplate) {
            DefaultMQProducer producer = rocketMQTemplate.getProducer();
            ExecutorService callbackExecutor = threadPoolFactory.createNormalThreadPool("RocketMQTemplateCallBackExecutor-" + beanName, 32);
            ExecutorService asyncSenderExecutor = threadPoolFactory.createNormalThreadPool("RocketMQTemplateAsyncSenderExecutor-" + beanName, 32);
            producer.setCallbackExecutor(callbackExecutor);
            producer.setAsyncSenderExecutor(asyncSenderExecutor);
            // 设置超时时间为5秒
            producer.setMqClientApiTimeout(5 * 1000);
            log.info("RocketMQTemplateBeanPostProcessor-postProcessAfterInitialization: {} producer: {}", beanName, producer);

        }
        return bean;
    }
}
