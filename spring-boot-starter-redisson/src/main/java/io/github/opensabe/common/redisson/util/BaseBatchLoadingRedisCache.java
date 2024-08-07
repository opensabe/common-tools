package io.github.opensabe.common.redisson.util;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.github.opensabe.common.utils.json.JsonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

public abstract class BaseBatchLoadingRedisCache<V extends BaseBatchLoadingRedisCache.Item> {
    public interface Item {
        String getKey();
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    protected abstract Class<V> getValueClass();

    protected abstract String getKey(String key);

    protected abstract List<V> batchLoadValues(Collection<String> keys);

    protected abstract long expireInSeconds();

    public List<V> batchGet(Collection<String> keys) {
        List<V> fromCache = stringRedisTemplate.executePipelined((RedisCallback<?>) action -> {
                    keys.stream().map(this::getKey)
                            .forEach(key -> {
                                action.get(key.getBytes(StandardCharsets.UTF_8));
                    });
                    return null;
                }).stream().filter(Objects::nonNull)
                .map(Object::toString)
                .map(s -> JSON.parseObject(s, getValueClass()))
                .collect(Collectors.toList());
        if (fromCache.size() == keys.size()) {
            return fromCache;
        } else {
            List<String> fromCacheJointedIds = fromCache.stream().map(Item::getKey).collect(Collectors.toList());
            Collection<String> subtract = CollectionUtils.subtract(keys, fromCacheJointedIds);
            //需要排序获取锁，防止死锁
            List<RLock> rLocks = subtract.stream()
                    .sorted()
                    .map(s -> redissonClient.getLock("lock:" + getKey(s)))
                    .collect(Collectors.toList());
            rLocks.forEach(rLock -> rLock.lock(expireInSeconds(), TimeUnit.SECONDS));
            List<V> loadValues = Lists.newArrayList();
            try {
                Collection<String> finalSubtract = subtract;
                //再查一遍缓存
                List<V> subtractFromCache = stringRedisTemplate.executePipelined((RedisCallback<?>) action -> {
                            finalSubtract.stream().map(this::getKey)
                                    .forEach(key -> {
                                        action.get(key.getBytes(StandardCharsets.UTF_8));
                                    });
                            return null;
                        }).stream().filter(Objects::nonNull)
                        .map(Object::toString)
                        .map(s -> JSON.parseObject(s, getValueClass()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(subtractFromCache)) {
                    loadValues.addAll(subtractFromCache);
                }
                subtract = CollectionUtils.subtract(
                        subtract,
                        subtractFromCache.stream()
                        .map(V::getKey)
                        .collect(Collectors.toList())
                );
                if (CollectionUtils.isNotEmpty(subtract)) {
                    batchLoadValues(subtract).stream()
                            .filter(Objects::nonNull)
                            .forEach(loadValues::add);
                }
            } finally {
                rLocks.stream()
                        .filter(RLock::isHeldByCurrentThread)
                        .forEach(Lock::unlock);
            }
            if (CollectionUtils.isNotEmpty(loadValues)) {
                fromCache.addAll(loadValues);
                stringRedisTemplate.executePipelined((RedisCallback<?>) action -> {
                    loadValues.forEach(loadValue -> {
                        action.setEx(
                                getKey(loadValue.getKey()).getBytes(StandardCharsets.UTF_8),
                                expireInSeconds(),
                                JsonUtil.toJSONString(loadValue).getBytes(StandardCharsets.UTF_8)
                        );
                    });
                    return null;
                });
            }
            return fromCache;
        }
    }
}
