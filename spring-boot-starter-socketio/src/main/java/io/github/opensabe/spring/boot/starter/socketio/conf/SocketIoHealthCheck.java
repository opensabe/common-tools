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
package io.github.opensabe.spring.boot.starter.socketio.conf;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.TimeUnit;

@Log4j2
public class SocketIoHealthCheck {
    public static volatile long lastDispatchMessage = System.currentTimeMillis();

    public SocketIoHealthCheck() {
        new Thread(() -> {
            try {
                for (long i = 0; i >= 0; i++) {
                    long now = System.currentTimeMillis();
                    long interval = now - lastDispatchMessage;
                    if (interval > 5 * 60 * 1000L) {
                        log.error("SocketIoHealthCheck-init [health thread] [over 5m not receive dispatch message]");
                    }
                    TimeUnit.SECONDS.sleep(30L);
                }
            } catch (Throwable e) {
                log.error("SocketIoHealthCheck-init [health thread] error", e);
            }

        }).start();
    }
}
