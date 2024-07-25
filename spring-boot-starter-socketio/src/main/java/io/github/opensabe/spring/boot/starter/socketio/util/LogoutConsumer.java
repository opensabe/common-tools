package io.github.opensabe.spring.boot.starter.socketio.util;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketIOClient;
import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.spring.boot.starter.rocketmq.AbstractMQConsumer;
import io.github.opensabe.spring.boot.starter.socketio.SocketIoMessageTemplate;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import java.util.Objects;

/**
 * 收到退出登录的消息以后强行断开连接
 * @author hengma
 * @time 2023/9/26 14:54
 */
@Log4j2
@RocketMQMessageListener(
        consumerGroup = "${spring.application.name}_" + ForceDisconnectProducer.MQ_TOPIC_LOGOUT + "_SuperKickForceDisconnect",
        consumeMode = ConsumeMode.CONCURRENTLY,
        topic = ForceDisconnectProducer.MQ_TOPIC_LOGOUT,
        messageModel = MessageModel.BROADCASTING
)
public class LogoutConsumer extends AbstractMQConsumer {

    private final SocketIoMessageTemplate socketIoMessageTemplate;

    public LogoutConsumer(SocketIoMessageTemplate socketIoMessageTemplate) {
        this.socketIoMessageTemplate = socketIoMessageTemplate;
    }

    @Override
    protected void onBaseMQMessage(BaseMQMessage baseMQMessage) {
        var dto = JSONObject.parseObject(baseMQMessage.getData(), ForceDisconnectProducer.ForceDisconnectDTO.class);
        if (Objects.nonNull(dto) && StringUtils.isNotBlank(dto.getUserId())) {
            var clients = socketIoMessageTemplate.getUserClients(dto.getUserId());
            if (CollectionUtils.isNotEmpty(clients)) {
                log.info("LogoutConsumer.onBaseMQMessage find clients size {} by userId", clients.size());
                clients.forEach(SocketIOClient::disconnect);
            }
        }
    }
}

