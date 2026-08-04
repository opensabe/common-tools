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
package io.github.opensabe.common.s3.typehandler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.convert.PropertyValueConverter;
import org.springframework.data.convert.ValueConversionContext;
import org.springframework.data.core.TypeInformation;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.http.MediaType;

import cn.hutool.core.codec.Hashids;
import io.github.opensabe.common.s3.properties.S3Properties;
import io.github.opensabe.common.s3.service.FileService;
import io.github.opensabe.common.utils.json.JsonUtil;
import tools.jackson.core.type.TypeReference;

/**
 * 将复杂属性以 JSON 文件存入 S3，持久化层仅保存对象键。
 * <p>
 * 路径模板 {@code {profile}/converter/{OwnerSimpleName}/{propertyName}/{hashids}.json}；
 * 序列化经 {@link JsonUtil}。
 *
 * @author heng.ma
 */
@SuppressWarnings("rawtypes")
public class S3JsonConverter implements PropertyValueConverter<Object, String, ValueConversionContext<?>> {

    /** 文件读写服务。 */
    private final FileService service;

    /** S3 对象键格式字符串（含三个 {@code %s} 占位符）。 */
    private final String fileName;

    /** 生成唯一文件名片段的 Hashids。 */
    private final Hashids hashids;

    /**
     * @param service S3 文件服务
     * @param properties 须含非空 {@link S3Properties#getProfile()}
     * @throws IllegalArgumentException profile 为空
     */
    public S3JsonConverter(FileService service, S3Properties properties) {
        this.service = service;
        this.hashids = Hashids.create("swdfffqssasd".toCharArray());
        String profile = properties.getProfile();
        if (StringUtils.isNotBlank(profile)) {
            this.fileName = profile + "/converter/%s/%s/%s.json";
        } else {
            throw new IllegalArgumentException("S3 profile must not be blank");
        }
    }

    /**
     * 按 S3 键读取 JSON 并反序列化。
     *
     * @param value 对象键
     * @param context 属性类型上下文
     * @return 反序列化对象
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object read(@NotNull String value, ValueConversionContext context) {
        PersistentProperty property = context.getProperty();
        TypeInformation typeInformation = property.getTypeInformation();
        byte[] bytes = service.getObject(value);
        return JsonUtil.parseObject(bytes, JacksonParameterizedTypeTypeReference.fromTypeInformation(typeInformation));
    }

    /**
     * 上传 JSON 并返回 S3 对象键。
     *
     * @param value 待序列化对象
     * @param context 属性上下文（用于生成路径）
     * @return 对象键
     */
    @Override
    public String write(@NotNull Object value, ValueConversionContext context) {
        PersistentProperty property = context.getProperty();
        String key = getFileName(property);
        service.putObjectAssignedPath(JsonUtil.toJSONBytes(value), key, MediaType.APPLICATION_JSON_VALUE);
        return key;
    }

    /**
     * 根据实体类名、属性名与时间戳生成唯一对象键。
     *
     * @param property 持久化属性
     * @return S3 键
     */
    private String getFileName(PersistentProperty property) {
        return fileName.formatted(property.getOwner().getType().getSimpleName(),
                property.getName(),
                hashids.encode(System.nanoTime(), Thread.currentThread().threadId()));
    }

    /**
     * 从 {@link TypeInformation} 构建 Jackson 参数化类型引用。
     *
     * @param <T> 目标类型
     */
    private static class JacksonParameterizedTypeTypeReference<T> extends TypeReference<T> {

        /** 参数化类型。 */
        private final ParameterizedType type;

        /**
         * @param information 属性类型信息
         */
        JacksonParameterizedTypeTypeReference(final TypeInformation<T> information) {
            final List<TypeInformation<?>> arguments = information.getTypeArguments();
            this.type = new ParameterizedType() {
                public Type @NotNull [] getActualTypeArguments() {
                    return arguments.stream().map(t -> t.toTypeDescriptor().getResolvableType().getType()).toArray(Type[]::new);
                }

                public @NotNull Type getRawType() {
                    return information.getType();
                }

                /**
                 * @return ownerType
                 */
                public Type getOwnerType() {
                    return null;
                }
            };
        }

        /**
         * 工厂方法。
         *
         * @param typeInformation 类型信息
         * @param <T> 目标类型
         * @return 类型引用
         */
        public static <T> JacksonParameterizedTypeTypeReference<T> fromTypeInformation(TypeInformation<T> typeInformation) {
            return new JacksonParameterizedTypeTypeReference<>(typeInformation);
        }

        /** {@inheritDoc} */
        public Type getType() {
            return this.type;
        }
    }
}
