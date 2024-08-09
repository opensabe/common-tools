package io.github.opensabe.spring.boot.starter.socketio.util;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import io.github.opensabe.spring.boot.starter.socketio.CommonAttribute;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

@Log4j2
public class SocketConnectionUtil {

    private SocketIOServer socketIOServer;

    private ForceDisconnectProducer forceDisconnectProducer;

    public SocketConnectionUtil(SocketIOServer socketIOServer, ForceDisconnectProducer forceDisconnectProducer) {
        this.socketIOServer = socketIOServer;
        this.forceDisconnectProducer = forceDisconnectProducer;
    }

    /**
     * @param gameFLg       游戏标识
     * @param currentClient 当前连接
     * @param forceTypeEnum 强制下线类型
     * @see ForceTypeEnum
     */
    public void forceDisconnect(String gameFLg, SocketIOClient currentClient, ForceTypeEnum forceTypeEnum) throws Exception {
        HttpHeaders httpHeaders = currentClient.getHandshakeData().getHttpHeaders();
        String platform = httpHeaders.get(CommonAttribute.PLATFORM);
        String userId = httpHeaders.get(CommonAttribute.UID);
        if (StringUtils.isBlank(userId)) {
            throw new Exception("userId is required");
        }
        String operatorId = httpHeaders.get(CommonAttribute.OPERATOR_ID);
        // 一个用户只能建立一个连接
        UUID sessionId = currentClient.getSessionId();
        String checkPlatformRoom = gameFLg + '_' + userId;
        if (ForceTypeEnum.ONE_USER_ONE_PLATFORM.equals(forceTypeEnum)) {
            if (StringUtils.isBlank(platform)) {
                throw new Exception("platform is required");
            }
            checkPlatformRoom += '_' + platform;
        }
        currentClient.joinRoom(checkPlatformRoom);
        log.info("SocketConnectionUtil-forceDisconnect [send force disconnect message] userId:{}, sessionId:{}, " +
                "roomId:{}", userId, sessionId, checkPlatformRoom);
        forceDisconnectProducer.sendForceDisconnectMsg(userId, sessionId, checkPlatformRoom);
    }

    public enum ForceTypeEnum {
        ONE_USER_ALL_PLATFORM,
        ONE_USER_ONE_PLATFORM;
    }

    public void logout (String userId) {
        forceDisconnectProducer.logout(userId);
    }
}
