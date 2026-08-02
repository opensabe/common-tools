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
package io.github.opensabe.spring.boot.starter.socketio.tracing.jfr;

import io.github.opensabe.spring.boot.starter.socketio.tracing.observation.SocketIOExecuteContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

/**
 * Socket.IO 断连事件 JFR 记录。
 */
@Getter
@Setter
@Category({"observation", "Socket-Disconnect"})
@Label("Disconnect Event")
@StackTrace(value = false)
public class SocketIODisConnectEvent extends Event {

/** traceId。 */
    private String traceId;

/** spanId。 */
    private String spanId;

/** sessionId。 */
    private String sessionId;

/** headers。 */
    private String headers;

    public SocketIODisConnectEvent(SocketIOExecuteContext context) {
        this.sessionId = context.getSocketIOClient().getSessionId().toString();
        this.headers = context.getAllHeaders();
    }
}