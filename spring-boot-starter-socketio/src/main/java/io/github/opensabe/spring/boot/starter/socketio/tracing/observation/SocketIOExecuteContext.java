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
package io.github.opensabe.spring.boot.starter.socketio.tracing.observation;

import com.corundumstudio.socketio.SocketIOClient;

import io.github.opensabe.spring.boot.starter.socketio.tracing.EventEnum;
import io.github.opensabe.spring.boot.starter.socketio.tracing.common.SocketIOParam;
import io.micrometer.observation.Observation;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocketIOExecuteContext extends Observation.Context implements SocketIOParam {

    private SocketIOClient socketIOClient;
    private String eventName;
    private EventEnum eventEnum;

    public SocketIOExecuteContext(SocketIOClient socketIOClient, String eventName, EventEnum eventEnum) {
        this.socketIOClient = socketIOClient;
        this.eventName = eventName;
        this.eventEnum = eventEnum;
    }

    @Override
    public SocketIOClient currentSocketIOClient() {
        return socketIOClient;
    }

    @Override
    public HttpHeaders currentHttpHeaders() {
        return socketIOClient.getHandshakeData().getHttpHeaders();
    }
}
