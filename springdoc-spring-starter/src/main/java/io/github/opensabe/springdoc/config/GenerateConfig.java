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
package io.github.opensabe.springdoc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.springdoc.converters.DateTimeModelConverter;
import io.github.opensabe.springdoc.converters.VoidModelResolver;

/**
 * @author heng.ma
 */
/**
 * GenerateConfig 类。
 * <p>Generate Config。</p>
 */
@Configuration(proxyBeanMethods = false)
public class GenerateConfig {

/**
 * dateTimeModelConverter 方法。
 */
    @Bean
    @ConditionalOnMissingBean
    public DateTimeModelConverter dateTimeModelConverter() {
        return new DateTimeModelConverter();
    }

/**
 * voidModelResolver 方法。
 */
    @Bean
    @ConditionalOnMissingBean
    public VoidModelResolver voidModelResolver() {
        return new VoidModelResolver();
    }

}
