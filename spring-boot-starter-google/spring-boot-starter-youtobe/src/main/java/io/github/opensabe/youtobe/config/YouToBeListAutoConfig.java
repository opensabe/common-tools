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
package io.github.opensabe.youtobe.config;

import io.github.opensabe.youtobe.properties.YouToBeDataApiProperties;
import io.github.opensabe.youtobe.service.YouToBeListService;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * YouToBeListService自动装配
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(YouToBeDataApiProperties.class)
public class YouToBeListAutoConfig {


    @Bean
    @ConditionalOnMissingBean
    public YouToBeListService youToBeListService(YouToBeDataApiProperties properties, OkHttpClient okHttpClient) {
        return new YouToBeListService(properties, okHttpClient);
    }
}
