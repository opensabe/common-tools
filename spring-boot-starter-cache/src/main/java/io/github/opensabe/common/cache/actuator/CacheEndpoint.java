package io.github.opensabe.common.cache.actuator;

import io.github.opensabe.common.cache.config.CachesProperties;
import io.github.opensabe.common.cache.utils.CacheHelper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Endpoint(id = "cache")
@AllArgsConstructor
public class CacheEndpoint {

    private static final Pattern COMPILE = Pattern.compile("::");
    private final CompositeCacheManager cacheManager;
    private final ApplicationContext context;
    private final CachesProperties cachesProperties;

    @ReadOperation
    public CacheReport allCacheNames(){
        return CacheReport.builder()
                .cacheType(null)
                .cacheName(null)
                .success(true)
                .message(null)
                .data(cacheManager.getCacheNames().stream()
                        .map(cacheName -> cachesProperties.getCustom().stream()
                                .filter(cacheProperties -> cacheProperties.getCacheNames().contains(cacheName))
                                .findFirst()
                                .orElse(null)
                        ).map(cacheInfo -> {
                            if(cacheInfo != null){
                                Map<String, Object> infoMap = new HashMap<>(4);
                                infoMap.put("type", cacheInfo.getType());
                                infoMap.put("cacheDesc", cacheInfo.getCacheDesc());
                                infoMap.put("cacheNames", cacheInfo.getCacheNames().stream().map(name -> name.replace(CacheHelper.CACHE_NAME_PREFIX, "")).collect(Collectors.toList()));
                                infoMap.put("settings", cacheInfo.getCacheSetting());
                                return infoMap;
                            }
                            return null;
                        })
                        .collect(Collectors.toSet())
                ).build();
    }

    @ReadOperation
    public CacheReport cacheKeys(@Selector String cacheName, @Selector Long pageSize, @Selector Long pageNumber) {
        String hideCacheName = CacheHelper.CACHE_NAME_PREFIX + cacheName;
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return errorCacheReport(cacheName, "Cache not found");
        }else if (pageNumber <= 0 || pageSize <= 0) {
            return errorCacheReport(cacheName, "Page number and page size must be greater than 0");
        }

        String msg;
        Set<@NonNull Object> keys = new HashSet<>(0);
        if (cache instanceof RedisCache){
            StringRedisTemplate template = context.getBean(StringRedisTemplate.class);
            Long hashSize = template.opsForHash().size(hideCacheName);
            long selectSize = Math.min(pageSize * pageNumber, hashSize);
            try(Cursor<Map.Entry<Object,Object>> cursor = template.opsForHash().scan(hideCacheName, ScanOptions.scanOptions().match("*").count(10000).build())) {
                while (cursor.hasNext()) {
                    Map.Entry<Object, Object> entry = cursor.next();
                    keys.add(entry.getKey());
                    if (keys.size() >= selectSize) {
                        break;
                    }
                }
            }
            msg = "Size: " + hashSize;
        }else if (cache instanceof CaffeineCache){
            keys = ((CaffeineCache)cache).getNativeCache().asMap().keySet();
            //keys.size() will have a bug when if the key expire
            msg = "Size: " + keys.toArray().length;
        }else{
            msg = "Not supported cache type";
        }

        Set<Object> rst = keys.stream().skip(pageSize * (pageNumber - 1)).limit(pageSize).collect(Collectors.toSet());
        return cacheReport(cacheName, cache.getClass().getSimpleName(), msg, rst);
    }

    @ReadOperation
    public CacheReport cacheValue(@Selector String cacheName, @Selector String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return errorCacheReport(cacheName, "Cache not found");
        }

        Cache.ValueWrapper cacheValue = cache.get(redisOrCaffeineKey(key));
        if(cacheValue == null){
            return errorCacheReport(cacheName, "Key not found");
        }

        return cacheReport(cacheName, cache.getClass().getSimpleName(), null, Collections.singleton(cacheValue.get()));
    }

    @DeleteOperation
    public CacheReport invalidateKey(@Selector String cacheName, @Selector String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return errorCacheReport(cacheName, "Cache not found");
        }
        
        if (cache instanceof RedisCache) {
            StringRedisTemplate template = context.getBean(StringRedisTemplate.class);
            template.opsForHash().delete(CacheHelper.CACHE_NAME_PREFIX + cacheName, key);
        }
        cache.evict(redisOrCaffeineKey(key));
        return cacheReport(cacheName, cache.getClass().getSimpleName(), null, Collections.singleton(key));
    }

    @DeleteOperation
    @SuppressWarnings("unchecked")
    public CacheReport evictCache(@Selector String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return errorCacheReport(cacheName, "Cache not found");
        }

        if (cache instanceof RedisCache){
            ((Set<Object>)cacheKeys(cacheName, Long.MAX_VALUE, 1L).getData()).forEach(key -> invalidateKey(cacheName, key.toString()));
        }else {
            cache.clear();
        }
        return cacheReport(cacheName, cache.getClass().getSimpleName(), null, Collections.emptySet());
    }

    private CacheReport cacheReport(String cacheName, String cacheType, String msg, Object body) {
        return new CacheReport.CacheReportBuilder().cacheType(cacheType).cacheName(cacheName).success(Boolean.TRUE).message(msg).data(body).build();
    }

    private CacheReport errorCacheReport(String cacheName, String error) {
        return new CacheReport.CacheReportBuilder().cacheType("Unknown").cacheName(cacheName).success(Boolean.FALSE).message(error).data(null).build();
    }

    private String redisOrCaffeineKey(String oKey){
        return oKey.contains("::")? COMPILE.split(oKey)[1] :oKey;
    }

    @Builder
    @Getter
    public static class CacheReport {
        private String cacheType;
        private String cacheName;
        private Boolean success;
        private String message;
        private Object data;
    }
}
