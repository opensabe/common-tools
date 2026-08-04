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
package io.github.opensabe.common.web.config;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.http.converter.autoconfigure.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.google.common.collect.Lists;

/**
 * HTTP 消息转换器配置。
 * <p>
 * 注册 Fastjson 与纯文本 {@link StringHttpMessageConverter}；
 * {@link LocalDateTime} 序列化为系统时区 epoch 毫秒时间戳。
 */
@Configuration
public class ConverterConfiguration {

    /**
     * 创建 Fastjson 与 String 消息转换器组合。
     * <p>
     * Fastjson 自 1.2 起须显式声明 {@code application/json} 媒体类型；
     * 启用禁用循环引用检测、输出 null 字段与字段名引号等序列化特性。
     *
     * @return HTTP 消息转换器集合
     */
    @SuppressWarnings("deprecation")
    @Bean
    public HttpMessageConverters httpMessageConverters() {
        RevoFastJsonHttpMessageConverter fastJsonHttpMessageConverter =
                new RevoFastJsonHttpMessageConverter();

        List<MediaType> supportedMediaTypes = Lists.newArrayList();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        fastJsonHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);

        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        StringHttpMessageConverter stringHttpMessageConverter =
                new StringHttpMessageConverter(Charset.defaultCharset());
        stringHttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.TEXT_PLAIN));
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.QuoteFieldNames
        );

        fastJsonConfig.getSerializeConfig().put(LocalDateTime.class, (serializer, object, fieldName, fieldType, features) -> {
            if (object != null) {
                long timestamp = ((LocalDateTime) object).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                serializer.write(timestamp);
            }
        });

        return new HttpMessageConverters(fastJsonHttpMessageConverter, stringHttpMessageConverter);
    }
}
