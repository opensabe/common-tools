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
package io.github.opensabe.common.secret;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Sets;

import io.github.opensabe.common.utils.AlarmUtil;

/**
 * 全局敏感信息缓存与日志/输出过滤管理器。
 * <p>
 * 各 {@link SecretProvider} 定期将密钥快照写入本管理器；检测到敏感子串时会掩码并触发 {@link AlarmUtil#fatal} 告警。
 */
public class GlobalSecretManager {
    /** 各 {@link SecretProvider#name()} 到密钥快照的缓存。 */
    private final Cache<String, Map<String, Set<String>>> cache = Caffeine.newBuilder().build();

    /** 敏感子串替换掩码。 */
    public static final String MASKER = "******";

    /**
     * 将指定 Provider 的密钥快照写入缓存。
     *
     * @param secretName Provider 名称
     * @param secret     密钥名到敏感值集合的映射
     */
    void putSecret(String secretName, Map<String, Set<String>> secret) {
        cache.put(secretName, secret);
    }

    /**
     * 扫描内容中的敏感子串，命中时替换为 {@link #MASKER} 并触发 fatal 告警。
     *
     * @param content 待检查文本
     * @return 是否命中敏感信息及掩码后的内容
     */
    public FilterSecretStringResult filterSecretStringAndAlarm(String content) {
        boolean foundSensitiveString = false;
        String filteredContent = content;
        Set<String> foundKeys = Sets.newHashSet();

        for (Map.Entry<String, Map<String, Set<String>>> stringMapEntry : cache.asMap().entrySet()) {
            for (Map.Entry<String, Set<String>> stringSetEntry : stringMapEntry.getValue().entrySet()) {
                for (String sensitiveString : stringSetEntry.getValue()) {
                    if (StringUtils.contains(filteredContent, sensitiveString)) {
                        foundSensitiveString = true;
                        filteredContent = StringUtils.replace(filteredContent, sensitiveString, MASKER);
                        foundKeys.add("SecretProvider: " + stringMapEntry.getKey() + ", key: " + stringSetEntry.getKey());
                    }
                }
            }
        }

        if (foundSensitiveString) {
            AlarmUtil.fatal("Sensitive info detected: in content: {}, got keys: {}", filteredContent, String.join(",", foundKeys));
        }
        return FilterSecretStringResult.builder()
                .foundSensitiveString(foundSensitiveString)
                .filteredContent(filteredContent)
                .build();
    }
}
