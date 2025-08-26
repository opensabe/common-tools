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

public class GlobalSecretManager {
    private final Cache<String, Map<String, Set<String>>> cache = Caffeine.newBuilder().build();

    void putSecret(String secretName, Map<String, Set<String>> secret) {
        cache.put(secretName, secret);
    }

    /**
     * Filter sensitive string in content, and alarm if found
     *
     * @param content
     * @return
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
                        filteredContent = StringUtils.replace(filteredContent, sensitiveString, "******");
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
