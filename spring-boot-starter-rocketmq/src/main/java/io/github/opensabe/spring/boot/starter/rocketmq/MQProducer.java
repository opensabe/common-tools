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

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;

public interface MQProducer {
    /**
     * 同步发送消息，不能保持消费有序
     * @param topic 主题
     * @param o 消息体
     * 如果消息体为BaseMQMessage，则会填充其中的除了业务traceId还有action的所有字段
     * 不为BaseMQMessage会封装成为BaseMQMessage
     * @see BaseMQMessage
     */
    void send(String topic, Object o);

    void send(String topic, Object o,Long time);

    void send(String topic, Object o, MQSendConfig mqSendConfig);

    /**
     * 异步发送，
     * @param topic
     * @param o
     */
    void sendAsync(String topic, Object o);
    void sendAsync(String topic, Object o,Long time);
    void sendAsync(String topic, Object o, MQSendConfig mqSendConfig);

    /**
     * 可以通过isAsync是否同步发送消息
     * @param topic
     * @param o
     * @param isAsync
     */
    void send(String topic, Object o, boolean isAsync);

    void send(String topic, Object o, boolean isAsync,Long time);

    void send(String topic, Object o, boolean isAsync, MQSendConfig mqSendConfig);

    /**
     * 异步发送，设置回调
     * @param topic
     * @param o
     * @param sendCallback
     */
    void sendAsync(String topic, Object o, SendCallback sendCallback);
    void sendAsync(String topic, Object o, SendCallback sendCallback,Long time);
    void sendAsync(String topic, Object o, SendCallback sendCallback, MQSendConfig mqSendConfig);

    /**
     * 如果指定了hashKey，则会通过这个key进行分片发送到固定的queue上面，如果消费者是有序消费模式，则能保证同一个key下的消息有序
     * @param topic
     * @param o
     * @param hashKey
     */
    void send(String topic, Object o, String hashKey);
    void send(String topic, Object o, String hashKey,Long time);
    void send(String topic, Object o, String hashKey, MQSendConfig mqSendConfig);

    /**
     * 如果指定了hashKey，则会通过这个key进行分片发送到固定的queue上面，如果消费者是有序消费模式，则能保证同一个key下的消息有序
     * @param topic
     * @param o
     * @param hashKey
     */
    void sendAsync(String topic, Object o, String hashKey, SendCallback sendCallback);
    void sendAsync(String topic, Object o, String hashKey, SendCallback sendCallback,Long time);
    void sendAsync(String topic, Object o, String hashKey, SendCallback sendCallback, MQSendConfig mqSendConfig);

    /**
     *
     * @param topic
     * @param o
     * @param hashKey
     * @param isAsync
     */
    void send(String topic, Object o, String hashKey, boolean isAsync);
    void send(String topic, Object o, String hashKey, boolean isAsync,Long time);
    void send(String topic, Object o, String hashKey, boolean isAsync, MQSendConfig mqSendConfig);

    /**
     *
     * @param topic
     * @param o
     * @param hashKey
     * @param isAsync
     */
    void send(String topic, Object o, String hashKey, boolean isAsync, SendCallback sendCallback, MQSendConfig mqSendConfig);

    /**
     *
     * @param topic
     * @param o
     * @param hashKey
     * @param isAsync
     * @param time
     * @param sendCallback
     * @param mqSendConfig
     */
    void send(String topic, Object o, String hashKey, boolean isAsync,Long time, SendCallback sendCallback, MQSendConfig mqSendConfig);

    /**
     * 事务发送
     * @param topic 主题
     * @param body 消息体
     * @param transactionObj 本地事务对象
     */
    void sendWithInTransaction(String topic, Object body, Object transactionObj, UniqueRocketMQLocalTransactionListener uniqueRocketMQLocalTransactionListener);

    /**
     * 仅供重试使用
     * @param topic
     * @param hashKey
     * @param baseMQMessage
     * @param traceIdString
     * @return
     */
    SendResult sendWithoutRetry(String topic, String hashKey, String baseMQMessage, String traceIdString);
}
