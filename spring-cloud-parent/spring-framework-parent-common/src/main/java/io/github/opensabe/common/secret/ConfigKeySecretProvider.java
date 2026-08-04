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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import lombok.Setter;


/**
 * 指定配置键的密钥提供者：从 {@link Environment} 读取 key 对应值并纳入脱敏。
 */
public abstract class ConfigKeySecretProvider extends SecretProvider implements EnvironmentAware {

    /** Spring 环境，用于读取配置键值。 */
    @Setter
    private Environment environment;

    /**
     * @param globalSecretManager 全局密钥管理器
     */
    protected ConfigKeySecretProvider(GlobalSecretManager globalSecretManager) {
        super(globalSecretManager);
    }


    /**
     * 需要纳入脱敏的配置键集合。
     *
     * @return 配置 property 键名集合
     */
    protected abstract Set<String> keys();

    /** {@inheritDoc} */
    @Override
    protected Map<String, Set<String>> reload() {
        Map<String, Set<String>> map = new HashMap<>(keys().size());
        for (String key : keys()) {
            String value = environment.getProperty(key);
            if (StringUtils.isNotBlank(value)) {
                map.put(key, Set.of(value));
            }
        }
        return map;
    }
}
