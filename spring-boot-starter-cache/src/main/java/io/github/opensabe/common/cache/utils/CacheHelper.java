package io.github.opensabe.common.cache.utils;

import org.springframework.util.ReflectionUtils;

public class CacheHelper {

    public static final String CACHE_NAME_PREFIX = "sfccmr:";

    @SuppressWarnings("unchecked")
    public static  <T> T readFiled(String filed,Object source,Class<T> type){
        var field = ReflectionUtils.findField(source.getClass(),filed,type);
        ReflectionUtils.makeAccessible(field);
        return (T)ReflectionUtils.getField(field,source);
    }
}
