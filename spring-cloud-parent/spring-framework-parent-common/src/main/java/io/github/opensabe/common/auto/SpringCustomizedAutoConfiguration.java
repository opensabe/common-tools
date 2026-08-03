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
package io.github.opensabe.common.auto;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import tools.jackson.databind.json.JsonMapper;

import io.github.opensabe.common.config.JsonUtilSpringBridge;
import io.github.opensabe.common.config.SpringCommonUtilConfiguration;

/**
 * 通用 Spring 工具与密钥相关自动配置入口。
 * <p>
 * 在 {@link JacksonAutoConfiguration} 之后加载，以便 {@link JsonUtilSpringBridge}
 * 注入已构建的 {@link JsonMapper}。Jackson 模块注册见 {@link JacksonCustomizedAutoConfiguration}。
 */
@AutoConfiguration(after = JacksonAutoConfiguration.class)
@Import(SpringCommonUtilConfiguration.class)
public class SpringCustomizedAutoConfiguration {

    /**
     * 将 Boot {@link JsonMapper} 桥接到 {@link io.github.opensabe.common.utils.json.JsonUtil}。
     * <p>
     * Bean 定义放在 AutoConfiguration 本类上，保证 {@code after = JacksonAutoConfiguration}
     * 与 {@code @ConditionalOnBean} 的延迟求值生效。
     *
     * @param jsonMapper Boot 自动配置的 JsonMapper
     * @return JsonUtil 桥接 Bean
     */
    @Bean
    @ConditionalOnBean(JsonMapper.class)
    public JsonUtilSpringBridge jsonUtilSpringBridge(JsonMapper jsonMapper) {
        return new JsonUtilSpringBridge(jsonMapper);
    }
}
