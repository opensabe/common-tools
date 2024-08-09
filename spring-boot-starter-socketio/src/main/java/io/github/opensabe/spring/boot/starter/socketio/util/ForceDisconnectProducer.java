package io.github.opensabe.spring.boot.starter.socketio.util;

import io.github.opensabe.spring.boot.starter.rocketmq.MQProducer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;

import java.io.Serializable;
import java.util.UUID;

@Log4j2
public class ForceDisconnectProducer {

    public static final String MQ_TOPIC_FORCE_DISCONNECT = "force_disconnect";
    public static final String MQ_TOPIC_LOGOUT = "force_logout";

    private MQProducer mqProducer;

    public ForceDisconnectProducer(MQProducer mqProducer) {
        this.mqProducer = mqProducer;
    }

    public void sendForceDisconnectMsg(String userId, UUID session, String roomId) {
        mqProducer.sendAsync(MQ_TOPIC_FORCE_DISCONNECT, new ForceDisconnectDTO(userId, session, roomId), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("ForceDisconnectProducer-sendForceDisconnectMsg [success], msgId:{}", sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("ForceDisconnectProducer-sendForceDisconnectMsg [exception]", throwable);
            }
        });
    }

    /**
     * 用户在主站退出登录以后踢下线
     * @param userId
     * @author hengma
     * @time 2023/9/26 14:40
     */
    public void logout (String userId) {
        mqProducer.sendAsync(MQ_TOPIC_LOGOUT, new ForceDisconnectDTO(userId, null, null), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("ForceDisconnectProducer-logout [success], msgId:{}", sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("ForceDisconnectProducer-logout [exception]", throwable);
            }
        });
    }
    @Setter
    @Getter
    public static class ForceDisconnectDTO implements Serializable {
        private String userId;

        private UUID session;

        private String roomId;

        public ForceDisconnectDTO(String userId, UUID session, String roomId) {
            this.userId = userId;
            this.session = session;
            this.roomId = roomId;
        }
    }
}
