package io.github.opensabe.spring.boot.starter.rocketmq;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.entity.base.vo.BaseMessage;
import io.github.opensabe.common.utils.json.JsonUtil;
import org.apache.rocketmq.common.message.MessageExt;

import java.nio.charset.Charset;

public abstract class AbstractMQConsumer extends AbstractConsumer<String> {

    protected abstract void onBaseMQMessage(BaseMQMessage baseMQMessage);

    @Override
    protected void onBaseMessage(BaseMessage<String> baseMessage) {
        onBaseMQMessage((BaseMQMessage) baseMessage);
    }

    @Override
    protected BaseMessage<String> convert(MessageExt ext) {
        String payload = new String(ext.getBody(), Charset.defaultCharset());

        return JsonUtil.parseObject(MQMessageUtil.decode(payload), BaseMQMessage.class);
    }
}
