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
package io.github.opensabe.spring.boot.starter.socketio.util;

import com.corundumstudio.socketio.SocketIOClient;
import io.github.opensabe.spring.boot.starter.socketio.CommonAttribute;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

@Log4j2
public class SocketConnectionUtil {


    private ForceDisconnectProducer forceDisconnectProducer;

    public SocketConnectionUtil(ForceDisconnectProducer forceDisconnectProducer) {
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
