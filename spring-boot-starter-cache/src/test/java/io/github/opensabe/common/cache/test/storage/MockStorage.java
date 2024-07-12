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
