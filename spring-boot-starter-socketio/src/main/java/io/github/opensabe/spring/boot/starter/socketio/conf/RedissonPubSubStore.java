package io.github.opensabe.spring.boot.starter.socketio.conf;

import com.corundumstudio.socketio.store.pubsub.PubSubListener;
import com.corundumstudio.socketio.store.pubsub.PubSubMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubStore;
import com.corundumstudio.socketio.store.pubsub.PubSubType;
import io.netty.util.internal.PlatformDependent;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class RedissonPubSubStore implements PubSubStore {
    private final RedissonClient redissonPub;
    private final RedissonClient redissonSub;
    private final Long nodeId;
    private final String serviceName;
    private final SocketIoServerProperties socketIoServerProperties;

    private final ConcurrentMap<String, Queue<Integer>> map = PlatformDependent.newConcurrentHashMap();

    public RedissonPubSubStore(RedissonClient redissonPub, RedissonClient redissonSub, Long nodeId, SocketIoServerProperties socketIoServerProperties) {
        this.redissonPub = redissonPub;
        this.redissonSub = redissonSub;
        this.nodeId = nodeId;
        this.serviceName = socketIoServerProperties.getNameSpace();
        this.socketIoServerProperties = socketIoServerProperties;
    }

    @Override
    public void publish(PubSubType type, PubSubMessage msg) {
        msg.setNodeId(nodeId);
        redissonPub.getTopic(getTopicName(type.toString())).publish(msg);
    }

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

    @Override
    public void unsubscribe(PubSubType type) {
        String name = getTopicName(type.toString());
        Queue<Integer> regIds = map.remove(name);
        RTopic topic = redissonSub.getTopic(name);
        for (Integer id : regIds) {
            topic.removeListener(id);
        }
    }

    private String getTopicName(String name) {
        return serviceName + '-' + name;
    }

    @Override
    public void shutdown() {
    }

}
