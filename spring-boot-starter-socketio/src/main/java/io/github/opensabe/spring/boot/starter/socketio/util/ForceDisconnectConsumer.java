package io.github.opensabe.spring.boot.starter.socketio.util;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.spring.boot.starter.rocketmq.AbstractMQConsumer;
import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;


@Log4j2
@RocketMQMessageListener(
        consumerGroup = "${spring.application.name}_" + ForceDisconnectProducer.MQ_TOPIC_FORCE_DISCONNECT + "_SuperKickForceDisconnect",
        consumeMode = ConsumeMode.CONCURRENTLY,
        topic = ForceDisconnectProducer.MQ_TOPIC_FORCE_DISCONNECT,
        messageModel = MessageModel.BROADCASTING,
        consumeThreadNumber = 64
)
public class ForceDisconnectConsumer extends AbstractMQConsumer {

    private SocketIOServer socketIOServer;

    public ForceDisconnectConsumer(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;
    }

    @Override
    protected void onBaseMQMessage(BaseMQMessage baseMQMessage) {
        log.info("ForceDisconnectConsumer-onBaseMQMessage {}", JSON.toJSONString(baseMQMessage));
        ForceDisconnectProducer.ForceDisconnectDTO forceDisconnectDTO = JSON.parseObject(baseMQMessage.getData(),
                ForceDisconnectProducer.ForceDisconnectDTO.class);
        Collection<SocketIOClient> clients = socketIOServer.getRoomOperations(forceDisconnectDTO.getRoomId()).getClients();
        for (SocketIOClient socketIOClient : clients) {
            UUID existSession = socketIOClient.getSessionId();
            UUID currentSession = forceDisconnectDTO.getSession();
            String userId = forceDisconnectDTO.getUserId();
            if (!Objects.equals(currentSession, existSession)) {
                log.info("ForceDisconnectConsumer-onBaseMQMessage [force disconnected] userId:{}, currentSession:{}, existSession:{}", userId,
                        currentSession, existSession);
                socketIOClient.disconnect();
            }
        }
    }
}
