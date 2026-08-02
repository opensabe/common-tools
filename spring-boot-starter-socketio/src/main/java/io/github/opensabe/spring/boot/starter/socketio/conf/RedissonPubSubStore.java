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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import com.corundumstudio.socketio.store.pubsub.PubSubListener;
import com.corundumstudio.socketio.store.pubsub.PubSubMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubStore;
import com.corundumstudio.socketio.store.pubsub.PubSubType;

import io.netty.util.internal.PlatformDependent;

/**
 * RedissonPubSubStore。
 */
public class RedissonPubSubStore implements PubSubStore {
/** redissonPub。 */
    private final RedissonClient redissonPub;
/** redissonSub。 */
    private final RedissonClient redissonSub;
/** nodeId。 */
    private final Long nodeId;
/** serviceName。 */
    private final String serviceName;
/** socketIoServer 配置属性。 */
    private final SocketIoServerProperties socketIoServerProperties;

    private final ConcurrentMap<String, Queue<Integer>> map = PlatformDependent.newConcurrentHashMap();

    public RedissonPubSubStore(RedissonClient redissonPub, RedissonClient redissonSub, Long nodeId, SocketIoServerProperties socketIoServerProperties) {
        this.redissonPub = redissonPub;
        this.redissonSub = redissonSub;
        this.nodeId = nodeId;
        this.serviceName = socketIoServerProperties.getNameSpace();
        this.socketIoServerProperties = socketIoServerProperties;
    }

    /** {@inheritDoc} */
    @Override
    public void publish(PubSubType type, PubSubMessage msg) {
        msg.setNodeId(nodeId);
        redissonPub.getTopic(getTopicName(type.toString())).publish(msg);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends PubSubMessage> void subscribe(PubSubType type, final PubSubListener<T> listener, Class<T> clazz) {
        String name = getTopicName(type.toString());
        RTopic topic = redissonSub.getTopic(name);
        int regId = topic.addListener(PubSubMessage.class, new RedissonPubSubStoreMessageListener(nodeId, listener, type, socketIoServerProperties));

        Queue<Integer> list = map.get(name);
        if (list == null) {
            list = new ConcurrentLinkedQueue<Integer>();
            Queue<Integer> oldList = map.putIfAbsent(name, list);
            if (oldList != null) {
                list = oldList;
            }
        }
        list.add(regId);
    }

    /** {@inheritDoc} */
    @Override
    public void unsubscribe(PubSubType type) {
        String name = getTopicName(type.toString());
        Queue<Integer> regIds = map.remove(name);
        RTopic topic = redissonSub.getTopic(name);
        for (Integer id : regIds) {
            topic.removeListener(id);
        }
    }

    /**
     * @return topicName
     */
    private String getTopicName(String name) {
        return serviceName + '-' + name;
    }

    /** {@inheritDoc} */
    @Override
    public void shutdown() {
    }

}
