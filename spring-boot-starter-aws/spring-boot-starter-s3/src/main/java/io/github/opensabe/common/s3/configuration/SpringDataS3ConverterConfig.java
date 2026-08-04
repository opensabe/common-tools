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
package io.github.opensabe.common.s3.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.PersistentProperty;

import io.github.opensabe.common.s3.properties.S3Properties;
import io.github.opensabe.common.s3.service.FileService;
import io.github.opensabe.common.s3.typehandler.S3JsonConverter;

/**
 * Spring Data S3 自定义属性转换器注册配置。
 */
@ConditionalOnClass(PersistentProperty.class)
@Configuration(proxyBeanMethods = false)
public class SpringDataS3ConverterConfig {

    /**
     * 注册 {@link S3JsonConverter} Bean。
     *
     * @param fileService S3 文件服务
     * @param properties  S3 配置
     * @return JSON 属性转换器
     */
    @Bean
    @ConditionalOnMissingBean
    public S3JsonConverter s3JsonConverter(FileService fileService, S3Properties properties) {
        return new S3JsonConverter(fileService, properties);
    }

}
