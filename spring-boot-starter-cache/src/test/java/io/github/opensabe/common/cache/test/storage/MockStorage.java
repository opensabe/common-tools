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
package io.github.opensabe.common.cache.test.storage;

import io.github.opensabe.common.cache.test.entity.ItemObject;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class MockStorage {

    private final Map<Long, ItemObject> data = new ConcurrentHashMap<>(16);

    public void addItem(ItemObject item) {
        data.put(item.getId(), item);
    }

    public ItemObject getItem(Long id) {
        return data.get(id);
    }

    public void deleteItem(Long id) {
        data.remove(id);
    }

    public Map<Long, ItemObject> getData() {
        return data;
    }
}
