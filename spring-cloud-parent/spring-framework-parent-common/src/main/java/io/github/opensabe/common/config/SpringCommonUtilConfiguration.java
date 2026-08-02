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

import tools.jackson.databind.ObjectMapper;
import io.github.opensabe.common.secret.ConfigurationPropertiesSecretProvider;

import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.Log4jAppenderCheckSecretCheckFilter;
import io.github.opensabe.common.utils.SpringUtil;
import io.github.opensabe.common.utils.json.JsonUtil;

/**
 * 通用工具类、密钥管理与 JSON 工具相关的 Spring 配置。
 */
@Configuration(proxyBeanMethods = false)
public class SpringCommonUtilConfiguration {

    /**
     * 注册 {@link SpringUtil}，供非 Spring 管理的代码获取 ApplicationContext。
     *
     * @return Spring 上下文工具实例
     */
    @Bean
    public SpringUtil getSpringUtil() {
        return new SpringUtil();
    }

    /**
     * 全局密钥管理器，协调各 {@link io.github.opensabe.common.secret.SecretProvider}。
     *
     * @return 全局密钥管理器
     */
    @Bean
    public GlobalSecretManager globalSecretManager() {
        return new GlobalSecretManager();
    }

    /**
     * 扫描 {@code @ConfigurationProperties} 上 {@link io.github.opensabe.common.secret.SecretProperty} 注解的密钥提供者。
     *
     * @param globalSecretManager 全局密钥管理器
     * @return 配置属性密钥提供者
     */
    @Bean
    public ConfigurationPropertiesSecretProvider annotationSecretProvider(GlobalSecretManager globalSecretManager) {
        return new ConfigurationPropertiesSecretProvider(globalSecretManager);
    }

    /**
     * Log4j Appender 层面的密钥泄露检查过滤器。
     *
     * @return Log4j 密钥检查过滤器
     */
    @Bean
    public Log4jAppenderCheckSecretCheckFilter log4jAppenderCheckSecretCheckFilter() {
        return new Log4jAppenderCheckSecretCheckFilter();
    }

    /**
     * 当 Spring 容器中存在 {@link ObjectMapper} Bean 时，将其注入 {@link JsonUtil} 静态门面。
     *
     * @param objectMapper Spring 管理的 Jackson ObjectMapper
     * @return JsonUtil 实例（副作用为替换静态 mapper）
     */
    @Bean
    @ConditionalOnBean(ObjectMapper.class)
    public JsonUtil jsonUtil(ObjectMapper objectMapper) {
        return new JsonUtil(objectMapper);
    }
}
