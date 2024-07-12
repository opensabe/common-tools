package io.github.opensabe.spring.boot.starter.socketio.conf;

import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubListener;
import com.corundumstudio.socketio.store.pubsub.PubSubMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubType;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.listener.MessageListener;

@Log4j2
public class RedissonPubSubStoreMessageListener<T> implements MessageListener<PubSubMessage> {
    private final Long nodeId;
    final PubSubListener<T> listener;
    final SocketIoServerProperties socketIoServerProperties;

    public RedissonPubSubStoreMessageListener(Long nodeId, PubSubListener<T> listener, PubSubType type, SocketIoServerProperties socketIoServerProperties) {
        this.nodeId = nodeId;
        this.listener = listener;
        this.socketIoServerProperties = socketIoServerProperties;
    }

    @Override
    public void onMessage(CharSequence channel, PubSubMessage msg) {
        try {
            if (msg instanceof DispatchMessage) {
                boolean match =
                        StringUtils.isBlank(socketIoServerProperties.getHealthCheckPacketName()) ||
                        StringUtils.equalsIgnoreCase(((DispatchMessage) msg).getPacket().getName(), socketIoServerProperties.getHealthCheckPacketName());
                if (match) {
                    SocketIoHealthCheck.lastDispatchMessage = System.currentTimeMillis();
                }

            }
            if (!nodeId.equals(msg.getNodeId())) {
                listener.onMessage((T) msg);
            }
        } catch (Throwable e) {
            log.error("RedissonPubSubStoreMessageListener-onMessage: error: {}", e.getMessage(), e);
        }
    }
}
