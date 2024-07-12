package io.github.opensabe.spring.boot.starter.rocketmq;

import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
