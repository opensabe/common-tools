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
package io.github.opensabe.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabe.common.secret.ConfigurationPropertiesSecretProvider;

import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.Log4jAppenderCheckSecretCheckFilter;
import io.github.opensabe.common.utils.SpringUtil;
import io.github.opensabe.common.utils.json.JsonUtil;

@Configuration(proxyBeanMethods = false)
public class SpringCommonUtilConfiguration {
    @Bean
    public SpringUtil getSpringUtil() {
        return new SpringUtil();
    }

    @Bean
    public GlobalSecretManager globalSecretManager() {
        return new GlobalSecretManager();
    }

    @Bean
    public ConfigurationPropertiesSecretProvider annotationSecretProvider (GlobalSecretManager globalSecretManager) {
        return new ConfigurationPropertiesSecretProvider(globalSecretManager);
    }

    @Bean
    public Log4jAppenderCheckSecretCheckFilter log4jAppenderCheckSecretCheckFilter() {
        return new Log4jAppenderCheckSecretCheckFilter();
    }

    @Bean
    @ConditionalOnBean(ObjectMapper.class)
    public JsonUtil jsonUtil(ObjectMapper objectMapper) {
        return new JsonUtil(objectMapper);
    }
}
