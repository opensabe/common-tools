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

import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.utils.json.JsonUtil;

public abstract class UniqueRocketMQLocalTransactionListener implements RocketMQLocalTransactionListener {
    /**
     * 名称，通过这个标识用哪个 UniqueRocketMQLocalTransactionListener 处理对应的事务消息发送回调
     */
    public abstract String name();

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        BaseMQMessage baseMQMessage = JsonUtil.parseObject(new String((byte[]) message.getPayload()), BaseMQMessage.class);
        return executeLocalTransaction(baseMQMessage, o);
    }

    public abstract RocketMQLocalTransactionState executeLocalTransaction(BaseMQMessage message, Object o);

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        BaseMQMessage baseMQMessage = JsonUtil.parseObject(new String((byte[]) message.getPayload()), BaseMQMessage.class);
        return checkLocalTransaction(baseMQMessage);
    }

    protected abstract RocketMQLocalTransactionState checkLocalTransaction(BaseMQMessage baseMQMessage);
}
