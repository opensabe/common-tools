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

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 仅仅指定哪些key需要脱敏，系统自动将key对应的值添加进去
 * @author maheng
 */
public abstract class ConfigKeySecretProvider extends SecretProvider implements EnvironmentAware {

    @Setter
    private Environment environment;

    protected ConfigKeySecretProvider(GlobalSecretManager globalSecretManager) {
        super(globalSecretManager);
    }


    protected abstract Set<String> keys ();

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
