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
package io.github.opensabe.spring.cloud.parent.common.handler;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.MapMaker;

import io.github.opensabe.base.vo.IntValueEnum;

/**
 * 通过 {@link IntValueEnum#getValue()} 将 {@link RequestParam}、{@link RequestHeader}
 * 中的int值转换为枚举值
 *
 * @author heng.ma
 */
@ControllerAdvice
public class EnumConvertConfiguration {

    private static final IntValueEnumConverter CONVERTER = new IntValueEnumConverter();
    /** 已注册 {@link ConversionService} 的弱引用集合，防止重复注册 converter。 */
    private static final Set<ConversionService> REGISTERED_SERVICES = Collections
            .newSetFromMap(new MapMaker()
                    .weakKeys()
                    .makeMap());

    /**
     * 判断字符串是否为纯整数字符串。
     *
     * @param s 待检字符串
     * @return 是否为可解析的整数
     */
    public static boolean isInteger(String s) {
        if (StringUtils.isBlank(s)) {
            return false;
        }
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * 为每个 {@link WebDataBinder} 注册 {@link IntValueEnumConverter}（同一 {@link ConversionService} 仅注册一次）。
     *
     * @param dataBinder 数据绑定器
     */
    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        ConversionService service = dataBinder.getConversionService();
        if (service instanceof ConverterRegistry registry) {
            // Avoid duplicate converter registration across repeated InitBinder calls
            if (REGISTERED_SERVICES.add(service)) {
                registry.addConverter(CONVERTER);
            }
        }
    }

    /**
     * {@link IntValueEnum} 的条件泛型转换器：支持 int/数字字符串/枚举名。
     */
    public static class IntValueEnumConverter implements ConditionalGenericConverter {
        /** {@inheritDoc} */
        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            return IntValueEnum.class.isAssignableFrom(targetType.getType());
        }

        /** {@inheritDoc} */
        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Set.of(new ConvertiblePair(String.class, IntValueEnum.class), new ConvertiblePair(Integer.class, IntValueEnum.class));
        }


        /**
         * 将 int/数字字符串/枚举名转为 {@link IntValueEnum}。
         *
         * @return 匹配的枚举常量，{@code source} 为 null 时返回 null
         */
        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (Objects.isNull(source)) {
                return null;
            }
            if (isInteger(source.toString())) {
                IntValueEnum[] enums = (IntValueEnum[]) IntValueEnum.values((Class) targetType.getType());
                try {
                    return Arrays.stream(enums)
                            .filter(e -> Objects.equals(Integer.valueOf(source.toString()), e.getValue()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "No enum constant " + targetType.getType().getCanonicalName() + "." + source));
                } catch (NumberFormatException e) {
                    return Enum.valueOf((Class) targetType.getType(), source.toString());
                }
            }
            return Enum.valueOf((Class) targetType.getType(), source.toString());
        }
    }
}
