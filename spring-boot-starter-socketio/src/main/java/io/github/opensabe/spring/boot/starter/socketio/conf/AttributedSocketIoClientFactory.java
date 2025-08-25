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

import com.corundumstudio.socketio.SocketIOClient;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Maps;
import io.github.opensabe.spring.boot.starter.socketio.AttributedSocketIoClient;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public class AttributedSocketIoClientFactory {
    private final Map<SocketIOClient, AttributedSocketIoClient> cache = Maps.newConcurrentMap();
    private final Cache<SocketIOClient, AttributedSocketIoClient> forRemoval = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .<SocketIOClient, AttributedSocketIoClient>removalListener((key, value, cause) -> {
                log.info("AttributedSocketIoClientFactory, remove: {}", key.getSessionId());
                cache.remove(key);
            }).build();

    public AttributedSocketIoClientFactory() {
        ScheduledExecutorService scheduledExecutorService = Executors
                .newScheduledThreadPool(1, r -> {
                    Thread thread = new Thread(r);
                    thread.setName("AttributedSocketIoClientFactory clean cache thread");
                    thread.setDaemon(true);
                    return thread;
                });
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            cache.entrySet().forEach(entry -> {
                if (!entry.getKey().isChannelOpen()) {
                    //需要放入清理队列，等一会再清理，防止业务使用 OnDisconnect 的时候读取不到
                    log.info("AttributedSocketIoClientFactory, add to forRemoval: {}", entry.getKey().getSessionId());
                    forRemoval.put(entry.getKey(), entry.getValue());
                }
            });
        }, 1, 1, java.util.concurrent.TimeUnit.MINUTES);
    }

    /**
     * 这里在 OnConnect 时候放入
     * @param socketIOClient
     * @return
     */
    AttributedSocketIoClient addSocketIoClient(SocketIOClient socketIOClient) {
        AttributedSocketIoClient attributedSocketIoClient = new AttributedSocketIoClient(socketIOClient);
        cache.put(socketIOClient, attributedSocketIoClient);
        return attributedSocketIoClient;
    }

    public AttributedSocketIoClient getAttributedSocketIoClient(SocketIOClient socketIOClient) {
        return cache.get(socketIOClient);
    }

    public String getSocketIoClientUserId(SocketIOClient socketIOClient) {
        AttributedSocketIoClient attributedSocketIoClient = cache.get(socketIOClient);
        if (attributedSocketIoClient != null) {
            return attributedSocketIoClient.getUserId();
        }
        return null;
    }

}
