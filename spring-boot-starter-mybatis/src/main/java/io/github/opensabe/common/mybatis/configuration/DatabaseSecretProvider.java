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
package io.github.opensabe.common.mybatis.configuration;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.opensabe.common.mybatis.properties.SqlSessionFactoryProperties;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DatabaseSecretProvider extends SecretProvider {
    private final SqlSessionFactoryProperties sqlSessionFactoryProperties;
    protected DatabaseSecretProvider(GlobalSecretManager globalSecretManager, SqlSessionFactoryProperties sqlSessionFactoryProperties) {
        super(globalSecretManager);
        this.sqlSessionFactoryProperties = sqlSessionFactoryProperties;
    }

    @Override
    protected String name() {
        return "spring-boot-starter-mybatis";
    }

    @Override
    protected long reloadTimeInterval() {
        return 1;
    }

    @Override
    protected TimeUnit reloadTimeIntervalUnit() {
        return TimeUnit.DAYS;
    }

    @Override
    protected Map<String, Set<String>> reload() {
        Map<String, Set<String>> result = Maps.newHashMap();
        sqlSessionFactoryProperties.getConfig().entrySet().stream().forEach(entry -> {
            String clusterName = entry.getKey();
            SqlSessionFactoryProperties.DatasourceConfiguration value = entry.getValue();
            Set<String> secrets = Sets.newHashSet();
            value.getDataSource().forEach(dataSourceProperties -> {
                secrets.add(dataSourceProperties.getPassword());
            });
            result.put(clusterName, secrets);
        });
        return result;
    }
}
