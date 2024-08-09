package io.github.opensabe.common.secret;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Sets;
import io.github.opensabe.common.utils.AlarmUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;

public class GlobalSecretManager {
    private final Cache<String, Map<String, Set<String>>> cache = Caffeine.newBuilder().build();

    void putSecret(String secretName, Map<String, Set<String>> secret) {
        cache.put(secretName, secret);
    }

    /**
     * Filter sensitive string in content, and alarm if found
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
