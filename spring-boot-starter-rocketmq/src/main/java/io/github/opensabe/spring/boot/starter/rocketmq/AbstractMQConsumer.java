package io.github.opensabe.spring.boot.starter.rocketmq;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.entity.base.vo.BaseMessage;

public abstract class AbstractMQConsumer extends AbstractConsumer<String> {

    protected abstract void onBaseMQMessage(BaseMQMessage baseMQMessage);

    @Override
    protected void onBaseMessage(BaseMessage<String> baseMessage) {
        onBaseMQMessage((BaseMQMessage) baseMessage);
    }
}
