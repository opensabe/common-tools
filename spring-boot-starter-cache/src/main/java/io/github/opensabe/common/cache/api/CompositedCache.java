package io.github.opensabe.common.cache.api;

import org.springframework.cache.Cache;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * @author heng.ma
 */
public class CompositedCache implements Cache {

    private final String name;
    private final Collection<Cache> list;

    public CompositedCache(String name, Collection<Cache> list) {
        this.name = name;
        this.list = list;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueWrapper get(Object key) {
       throw new UnsupportedOperationException();
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void evict(Object key) {
        list.forEach(cache -> cache.evict(key));
    }

    @Override
    public void clear() {
        list.forEach(Cache::clear);
    }
}
