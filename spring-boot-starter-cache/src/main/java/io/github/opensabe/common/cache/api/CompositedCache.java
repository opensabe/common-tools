package io.github.opensabe.common.cache.api;

import org.springframework.cache.Cache;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * 仅支持删除的缓存，如果遇到 <code>@CacheEvict</code>,不会指定
 * <code>@Expire</code>,此时CacheManager只会调用
 * <br>
 * <code>getCache(cacheName)</code>,因此，要返回该Cache去支持evict操作。
 * <p>
 *     <b>
 *     因为ExpireCacheManager都是动态创建缓存的，而evict操作仅需要当前已经存在的缓存，
 *     因此ExpireCacheManager在返回该缓存时，只需要添加当前已经创建的cache即可。
 *     </b>
 * </p>
 * @see org.springframework.cache.CacheManager#getCache(String)
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
