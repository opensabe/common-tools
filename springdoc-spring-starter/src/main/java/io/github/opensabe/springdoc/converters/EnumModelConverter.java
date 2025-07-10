package io.github.opensabe.springdoc.converters;

import io.github.opensabe.base.vo.IntValueEnum;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.PrimitiveType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.core.MethodParameter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 生成swagger文档时处理枚举类型，description加上各个int对应的枚举值
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
            schema.setDescription(map.entrySet().stream().map(e -> e.getKey()+"-"+e.getValue()).collect(Collectors.joining(",")));
            schema.setEnum(List.copyOf(map.keySet()));
            schema.setExamples(schema.getEnum());
            return schema;
        }
        return chain.hasNext()? chain.next().resolve(type, context, chain) : null;
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
            }else {
                parameterModel.setDescription(parameterModel.getDescription()+"("+schema.getDescription()+")");
            }
        }
        return parameterModel;
    }
}
