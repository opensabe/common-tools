package io.github.opensabe.common.cache.test.service;

import io.github.opensabe.common.cache.api.Expire;
import io.github.opensabe.common.cache.test.entity.ItemObject;
import io.github.opensabe.common.cache.test.storage.MockStorage;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class CacheService {

    private MockStorage storage;

    @Cacheable(value = "test_caffeine")
    public Map<Long, ItemObject> getData() {
        return storage.getData();
    }

    @Expire(5)
    @Cacheable(value = "test_caffeine")
    public ItemObject getCaffeineExpire(Long id, String field) {
        return storage.getItem(id);
    }
    @Expire(5)
    @Cacheable(value = "test_redis")
    public ItemObject getRedisExpire(Long id, String field) {
        return storage.getItem(id);
    }

    @Cacheable(value = "test_caffeine")
    public ItemObject getItemFromCaffeineWithoutKey(Long id, String field) {
        return storage.getItem(id);
    }

    @Cacheable(value = "test_caffeine", key = "#id")
    public ItemObject getItemFromCaffeine(Long id) {
        return storage.getItem(id);
    }

    @Cacheable(value = "test_redis", key = "'test_redis:'+#id")
    public String getItemFromRedis(Long id) {
        return storage.getItem(id).getName();
    }

    @Cacheable(value = "test_redis2", key = "'test_redis2:'+#id")
    public String getItemFromRedis2(Long id) {
        return storage.getItem(id).getName();
    }

    @CachePut(value = "test_caffeine", key = "#id")
    public ItemObject updateItemFromCaffeine(Long id) {
        return storage.getItem(id);
    }

    @CachePut(value = "test_redis", key = "'test_redis:'+#id")
    public String updateItemFromRedis(Long id) {
        return storage.getItem(id).getName();
    }

    @CacheEvict(value = "test_caffeine", key = "#id")
    public void deleteItemFromCaffeine(Long id) {
        storage.deleteItem(id);
    }

    @CacheEvict(value = "test_redis", key = "'test_redis:'+#id")
    public void deleteItemFromRedis(Long id) {
        storage.deleteItem(id);
    }

    //错误示范
    //Production Redis 禁止了 Keys 命令， 因此@CacheEvict 没有指定Key的情况下 方法不能使用
    //一定会报错
    @CacheEvict(value = "test_redis")
    public void deleteAllItemFromRedis() {}
}
