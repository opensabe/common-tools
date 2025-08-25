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

import org.apache.rocketmq.common.consumer.ConsumeFromWhere;

/**
 *  以前ConsumeFromWhere是写在注解里，这样得修改源码，侵入性比较强，不利于日后升级
 *  所以采用BeanPostProcessor的方式来扩展，如果要设置ConsumeFromWhere，只需要将
 *  我们的Consumer实现ConsumerAdjust接口即可
 * @author heng.ma
 */
public interface ConsumerAdjust {

    /**
     * @see org.apache.rocketmq.client.consumer.DefaultMQPushConsumer#setConsumeFromWhere(ConsumeFromWhere) 
     */
    ConsumeFromWhere consumeFromWhere ();


    /**
     * 当 consumeFromWhere = {@link ConsumeFromWhere#CONSUME_FROM_TIMESTAMP} 时有效，
     * 只消费 {consumeFromSecondsAgo} 秒之前的消息
     * @see org.apache.rocketmq.client.consumer.DefaultMQPushConsumer#setConsumeTimestamp(String)
     * @return
     */
    long consumeFromSecondsAgo();
}
