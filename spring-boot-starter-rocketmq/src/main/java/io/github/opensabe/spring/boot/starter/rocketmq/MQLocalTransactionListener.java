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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

import lombok.extern.log4j.Log4j2;

/**
 * 默认的 RocketMQLocalTransactionListener
 */
@Log4j2
@RocketMQTransactionListener
public class MQLocalTransactionListener implements RocketMQLocalTransactionListener {
    public static final String MSG_HEADER_TRANSACTION_LISTENER = "CustomizedTransactionListener";

    private final Map<String, UniqueRocketMQLocalTransactionListener> uniqueRocketMQLocalTransactionListeners;

    public MQLocalTransactionListener(List<UniqueRocketMQLocalTransactionListener> uniqueRocketMQLocalTransactionListeners) {
        this.uniqueRocketMQLocalTransactionListeners = uniqueRocketMQLocalTransactionListeners
                .stream().collect(Collectors.toMap(UniqueRocketMQLocalTransactionListener::name, v -> v));
    }

    private UniqueRocketMQLocalTransactionListener getUniqueRocketMQLocalTransactionListener(Message message) {
        Object transactionListenerName = message.getHeaders().get(MSG_HEADER_TRANSACTION_LISTENER);
        if (transactionListenerName == null) {
            log.fatal("MQLocalTransactionListener-executeLocalTransaction: MSG_HEADER_TRANSACTION_LISTENER not found");
            throw new RuntimeException("invalid transaction msg, MSG_HEADER_TRANSACTION_LISTENER not found");
        }
        String key = transactionListenerName.toString();
        UniqueRocketMQLocalTransactionListener uniqueRocketMQLocalTransactionListener = uniqueRocketMQLocalTransactionListeners.get(key);
        if (uniqueRocketMQLocalTransactionListener == null) {
            log.fatal("MQLocalTransactionListener-executeLocalTransaction: uniqueRocketMQLocalTransactionListener {} not found", key);
            throw new RuntimeException("invalid transaction msg, uniqueRocketMQLocalTransactionListener " + key + " not found");
        }
        log.info("MQLocalTransactionListener-executeLocalTransaction: found uniqueRocketMQLocalTransactionListener {}", uniqueRocketMQLocalTransactionListener.name());
        return uniqueRocketMQLocalTransactionListener;
    }

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        log.info("MQLocalTransactionListener-executeLocalTransaction: message: {}, o: {}", message, o);
        return getUniqueRocketMQLocalTransactionListener(message)
                .executeLocalTransaction(message, o);
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        log.info("MQLocalTransactionListener-checkLocalTransaction: message: {}", message);
        return getUniqueRocketMQLocalTransactionListener(message)
                .checkLocalTransaction(message);
    }
}
