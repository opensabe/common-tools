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
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;

import cn.hutool.core.codec.Hashids;
import io.github.opensabe.common.s3.properties.S3Properties;
import io.github.opensabe.common.s3.service.FileService;
import io.github.opensabe.common.utils.json.JsonUtil;

/**
 * @author heng.ma
 */
@SuppressWarnings("rawtypes")
public class S3JsonConverter implements PropertyValueConverter<Object, String, ValueConversionContext<?>> {

    private final FileService service;
    private final String fileName;

    private final Hashids hashids;

    public S3JsonConverter(FileService service, S3Properties properties) {
        this.service = service;
        this.hashids = Hashids.create("swdfffqssasd".toCharArray());
        String profile = properties.getProfile();
        if (StringUtils.isNotBlank(profile)) {
            this.fileName = profile + "/converter/%s/%s/%s.json";
        } else {
            throw new IllegalArgumentException("s3 profile must can not be null");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object read(@NotNull String value, ValueConversionContext context) {
        PersistentProperty property = context.getProperty();
        TypeInformation typeInformation = property.getTypeInformation();
        byte[] bytes = service.getObject(value);
        return JsonUtil.parseObject(bytes, JacksonParameterizedTypeTypeReference.fromTypeInformation(typeInformation));
    }

    @Override
    public String write(@NotNull Object value, ValueConversionContext context) {
        PersistentProperty property = context.getProperty();
        String key = getFileName(property);
        service.putObjectAssignedPath(JsonUtil.toJSONBytes(value), key, MediaType.APPLICATION_JSON_VALUE);
        return key;
    }

    private String getFileName(PersistentProperty property) {

        return fileName.formatted(property.getOwner().getType().getSimpleName(),
                property.getName(),
                hashids.encode(System.nanoTime(), Thread.currentThread().threadId()));
    }

    private static class JacksonParameterizedTypeTypeReference<T> extends TypeReference<T> {
        private final ParameterizedType type;

        JacksonParameterizedTypeTypeReference(final TypeInformation<T> information) {
            final List<TypeInformation<?>> arguments = information.getTypeArguments();
            this.type = new ParameterizedType() {
                public Type @NotNull [] getActualTypeArguments() {
                    return arguments.stream().map(t -> t.toTypeDescriptor().getResolvableType().getType()).toArray(Type[]::new);
                }

                public @NotNull Type getRawType() {
                    return information.getType();
                }

                public Type getOwnerType() {
                    return null;
                }
            };
        }

        public static <T> JacksonParameterizedTypeTypeReference<T> fromTypeInformation(TypeInformation<T> typeInformation) {
            return new JacksonParameterizedTypeTypeReference<>(typeInformation);
        }

        public Type getType() {
            return this.type;
        }
    }
}