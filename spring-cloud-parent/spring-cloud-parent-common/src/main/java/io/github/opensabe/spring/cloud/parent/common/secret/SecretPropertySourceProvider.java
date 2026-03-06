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
package io.github.opensabe.spring.cloud.parent.common.secret;

import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 将 secretPropertySource 中的配置脱敏。
 * @author maheng
 */
public class SecretPropertySourceProvider extends SecretProvider implements EnvironmentAware {

    private ConfigurableEnvironment environment;

    public SecretPropertySourceProvider(GlobalSecretManager globalSecretManager) {
        super(globalSecretManager);
    }


    @Override
    protected String name() {
        return "aesPropertiesTablePropertiesProvider";
    }

    @Override
    protected long reloadTimeInterval() {
        return 99;
    }

    @Override
    protected TimeUnit reloadTimeIntervalUnit() {
        return TimeUnit.MINUTES;
    }

    @Override
    protected Map<String, Set<String>> reload() {
        PropertySource<?> propertySource = environment.getPropertySources().get(AesPropertySourceResolver.SECRET_PROPERTY_SOURCE_NAME);
        if (propertySource instanceof EnumerablePropertySource<?> enumerablePropertySource) {
            Map<String, Set<String>> properties = new HashMap<>(1);
            Set<String> set = new HashSet<>(enumerablePropertySource.getPropertyNames().length);
            for (String propertyName : enumerablePropertySource.getPropertyNames()) {
                set.add(enumerablePropertySource.getProperty(propertyName).toString());
            }
            properties.put(name(), set);
            return properties;
        }
        return Map.of();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment instanceof ConfigurableEnvironment configurableEnvironment ? configurableEnvironment : null;
    }
}
