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
package io.github.opensabe.common.cache.test.service;

import java.util.Map;

import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import io.github.opensabe.common.cache.api.Expire;
import io.github.opensabe.common.cache.test.entity.ItemObject;
import io.github.opensabe.common.cache.test.storage.MockStorage;
import lombok.AllArgsConstructor;

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

    @CacheEvict(value = "test_caffeine")
    public void deleteCaffeine(Long id, String field) {
        storage.deleteItem(id);
    }

    @CacheEvict(value = "test_redis")
    public void deleteRedis(Long id, String field) {
        storage.deleteItem(id);
    }

    @Expire(5)
    @Cacheable(value = "test_redis")
    public ItemObject getRedisExpire(Long id, String field) {
        return storage.getItem(id);
    }

    @Expire(value = 5, cacheType = CacheType.CAFFEINE)
    @Cacheable(value = "test_redis")
    public ItemObject geAssignmentExpire(Long id, String field) {
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
    public void deleteAllItemFromRedis() {
    }
}
