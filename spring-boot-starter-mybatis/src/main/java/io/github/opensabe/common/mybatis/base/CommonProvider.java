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
package io.github.opensabe.common.mybatis.base;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.google.common.base.CaseFormat;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@SuppressWarnings({"rawtypes", "unchecked"})
public class CommonProvider<T> {
    private static final int CACHE_SIZE = 2 << 16;

    private static final Cache<String, String> camelToUnderScoreCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build();
    private static final Cache<String, String> underScoreToCamelCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build();
    private static final ThreadLocal<StringBuilder> stringBuilderThreadLocal = ThreadLocal.withInitial(() -> new StringBuilder());
    private static final ThreadLocal<CommonProvider> commonProviderThreadLocal = ThreadLocal.withInitial(() -> new CommonProvider<>());

    public static String camelToUnderScore(String name) {
        try {
            return camelToUnderScoreCache.get(name, () -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name));
        } catch (ExecutionException e) {
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        }
    }

    public static String underScoreToCamel(String name) {
        try {
            return underScoreToCamelCache.get(name, () -> CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name));
        } catch (ExecutionException e) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
        }
    }

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

    public static <T> CommonProvider<T> common() {
        return commonProviderThreadLocal.get();
    }
}
