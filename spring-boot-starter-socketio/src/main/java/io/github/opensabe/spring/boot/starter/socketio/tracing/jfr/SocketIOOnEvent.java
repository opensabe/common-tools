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

import com.corundumstudio.socketio.SocketIOClient;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Category({"observation", "Socket-OnEvent"})
@Label("On Event")
@StackTrace(value = false)
public class SocketIOOnEvent extends Event {

    private String traceId;

    private String spanId;

    private String sessionId;

    private String eventName;

    public SocketIOOnEvent(SocketIOClient socketIOClient, String eventName) {
        this.sessionId = socketIOClient.getSessionId().toString();
        this.eventName = eventName;
    }
}