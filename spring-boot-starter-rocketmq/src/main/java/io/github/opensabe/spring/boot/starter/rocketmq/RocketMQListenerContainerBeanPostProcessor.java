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

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 以前ConsumeFromWhere 是写在{@link org.apache.rocketmq.spring.annotation.RocketMQMessageListener}注解里，
 * 这样得修改源码，侵入性比较强，不利于日后升级
 * 所以采用BeanPostProcessor的方式来扩展，如果要设置ConsumeFromWhere，只需要将
 * 我们的Consumer实现{@link ConsumerAdjust}接口即可
 *
 * @author heng.ma
 */
public class RocketMQListenerContainerBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DefaultRocketMQListenerContainer container) {
            RocketMQListener listener = container.getRocketMQListener();
            if (listener instanceof ConsumerAdjust adjust) {
                DefaultMQPushConsumer consumer = container.getConsumer();
                ConsumeFromWhere consumeFromWhere = adjust.consumeFromWhere();
                consumer.setConsumeFromWhere(consumeFromWhere);
                if (ConsumeFromWhere.CONSUME_FROM_TIMESTAMP.equals(consumeFromWhere)) {
                    consumer.setConsumeTimestamp(UtilAll.timeMillisToHumanString3(System.currentTimeMillis() - (1000 * adjust.consumeFromSecondsAgo())));
                }
            }
        }
        return bean;
    }
}
