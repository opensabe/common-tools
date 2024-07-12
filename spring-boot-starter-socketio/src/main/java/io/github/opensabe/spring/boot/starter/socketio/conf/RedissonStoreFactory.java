package io.github.opensabe.spring.boot.starter.socketio.conf;

import com.corundumstudio.socketio.store.RedissonStore;
import com.corundumstudio.socketio.store.Store;
import com.corundumstudio.socketio.store.pubsub.BaseStoreFactory;
import com.corundumstudio.socketio.store.pubsub.PubSubStore;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.UUID;

public class RedissonStoreFactory extends BaseStoreFactory {
    private final RedissonClient redisClient;
    private final RedissonClient redisPub;
    private final RedissonClient redisSub;

    private final PubSubStore pubSubStore;

    public RedissonStoreFactory(RedissonClient redisson, SocketIoServerProperties socketIoServerProperties) {
        this.redisClient = redisson;
        this.redisPub = redisson;
        this.redisSub = redisson;

        this.pubSubStore = new RedissonPubSubStore(redisPub, redisSub, getNodeId(), socketIoServerProperties);
    }

    @Override
    public Store createStore(UUID sessionId) {
        return new RedissonStore(sessionId, redisClient);
    }

    @Override
    public PubSubStore pubSubStore() {
        return pubSubStore;
    }

    @Override
    public void shutdown() {
        redisClient.shutdown();
        redisPub.shutdown();
        redisSub.shutdown();
    }

    @Override
    public <K, V> Map<K, V> createMap(String name) {
        return redisClient.getMap(name);
    }
}
