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

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.module.blackbird.BlackbirdModule;

import io.github.opensabe.common.jackson.TimestampModule;

/**
 * Jackson 模块与 JsonMapper 定制相关的 Spring 配置。
 * <p>
 * 将自定义模块注册为 Spring Bean，避免 SPI（{@code findAndAddModules}）加载顺序不确定
 * 导致 JSR-310 覆盖 {@link java.time.LocalDateTime} 的时间戳序列化；并显式启用
 * {@link DateTimeFeature#WRITE_DATES_AS_TIMESTAMPS}，与 standalone {@code JsonUtil} 对齐。
 * <p>
 * 须在 {@code JacksonAutoConfiguration} <strong>之前</strong>加载，以便 Module Bean 进入
 * Boot 的 {@code Collection<JacksonModule>} 注入。
 */
@Configuration(proxyBeanMethods = false)
public class JacksonCustomizedConfiguration {

    /**
     * 注册时间戳序列化模块，使 {@link java.time.LocalDateTime} 以毫秒时间戳读写。
     *
     * @return {@link TimestampModule} 实例
     */
    @Bean
    public JacksonModule timestampModule() {
        return new TimestampModule();
    }

    /**
     * 注册 Blackbird 模块，通过预编译字节码提升序列化/反序列化性能。
     *
     * @return {@link BlackbirdModule} 实例
     */
    @Bean
    public JacksonModule blackbirdModule() {
        return new BlackbirdModule();
    }

    /**
     * 与 standalone {@code JsonUtil} 一致：日期按时间戳写出。
     *
     * @return JsonMapper builder 定制器
     */
    @Bean
    public JsonMapperBuilderCustomizer opensabeWriteDatesAsTimestampsCustomizer() {
        return builder -> builder.enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
