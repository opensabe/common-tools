package io.github.opensabe.spring.boot.starter.rocketmq;

import com.alibaba.fastjson.JSON;
import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

public abstract class UniqueRocketMQLocalTransactionListener implements RocketMQLocalTransactionListener {
    /**
     * 名称，通过这个标识用哪个 UniqueRocketMQLocalTransactionListener 处理对应的事务消息发送回调
     * @return
     */
    public abstract String name();

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        BaseMQMessage baseMQMessage = JSON.parseObject(new String((byte[]) message.getPayload()), BaseMQMessage.class);
        return executeLocalTransaction(baseMQMessage, o);
    }

    public abstract RocketMQLocalTransactionState executeLocalTransaction(BaseMQMessage message, Object o);

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        BaseMQMessage baseMQMessage = JSON.parseObject(new String((byte[]) message.getPayload()), BaseMQMessage.class);
        return checkLocalTransaction(baseMQMessage);
    }

    protected abstract RocketMQLocalTransactionState checkLocalTransaction(BaseMQMessage baseMQMessage);
}
