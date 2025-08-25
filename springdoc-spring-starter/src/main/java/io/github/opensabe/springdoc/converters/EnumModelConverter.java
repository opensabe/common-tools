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
package io.github.opensabe.springdoc.converters;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.core.MethodParameter;

import io.github.opensabe.base.vo.IntValueEnum;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.PrimitiveType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

/**
 * 生成swagger文档时处理枚举类型，description加上各个int对应的枚举值
 *
 * @author heng.ma
 */
public class EnumModelConverter implements ModelConverter, ParameterCustomizer {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        Class rawClass = Json.mapper().constructType(type.getType()).getRawClass();
        if (IntValueEnum.class.isAssignableFrom(rawClass) && rawClass.isEnum()) {
            Map<Integer, String> map = Arrays.stream(rawClass.getEnumConstants())
                    .collect(Collectors.toMap(e -> ((IntValueEnum) e).getValue(), e -> ((Enum) e).name()));
            Schema schema = PrimitiveType.INT.createProperty();
            schema.setDescription(map.entrySet().stream().map(e -> e.getKey() + "-" + e.getValue()).collect(Collectors.joining(",")));
            schema.setEnum(List.copyOf(map.keySet()));
            schema.setExamples(schema.getEnum());
            return schema;
        }
        return chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
    }

    @Override
    public Parameter customize(Parameter parameterModel, MethodParameter methodParameter) {
        if (Objects.isNull(parameterModel)) {
            return null;
        }
        Schema<?> schema = parameterModel.getSchema();
        if (Objects.nonNull(schema) && StringUtils.isNotBlank(schema.getDescription())) {
            if (StringUtils.isBlank(parameterModel.getDescription())) {
                parameterModel.setDescription(schema.getDescription());
            } else {
                parameterModel.setDescription(parameterModel.getDescription() + "(" + schema.getDescription() + ")");
            }
        }
        return parameterModel;
    }
}
