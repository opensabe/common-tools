package io.github.opensabe.common.mybatis.base;

import com.google.common.base.CaseFormat;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CommonProvider<T> {
    private static final int CACHE_SIZE = 2 << 16;

    private static final Cache<String, String> camelToUnderScoreCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build();

    public static String camelToUnderScore(String name) {
        try {
            return camelToUnderScoreCache.get(name, () -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name));
        } catch (ExecutionException e) {
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        }
    }

    private static final Cache<String, String> underScoreToCamelCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build();

    public static String underScoreToCamel(String name) {
        try {
            return underScoreToCamelCache.get(name, () -> CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name));
        } catch (ExecutionException e) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
        }
    }

    private static final ThreadLocal<StringBuilder> stringBuilderThreadLocal = ThreadLocal.withInitial(() -> new StringBuilder());

    public static String getFieldsFromStrCollection(Collection<String> strings) {
        StringBuilder stringBuilder = stringBuilderThreadLocal.get();
        stringBuilder.setLength(0);
        strings.forEach(string -> {
            stringBuilder.append(string).append(",");
        });
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    
	public static String getInStrFromStrCollection(Collection objects) {
        if (objects.isEmpty()) {
            return "('')";
        }
        StringBuilder stringBuilder = stringBuilderThreadLocal.get();
        stringBuilder.setLength(0);
        stringBuilder.append("(");
        objects.forEach(string -> {
            stringBuilder.append("'").append(string).append("',");
        });
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    private static final ThreadLocal<CommonProvider> commonProviderThreadLocal = ThreadLocal.withInitial(() -> new CommonProvider<>());

    public static <T> CommonProvider<T> common() {
        return commonProviderThreadLocal.get();
    }
}
