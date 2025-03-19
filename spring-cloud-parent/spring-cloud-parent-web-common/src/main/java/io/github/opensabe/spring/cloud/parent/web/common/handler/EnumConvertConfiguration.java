package io.github.opensabe.spring.cloud.parent.web.common.handler;

import io.github.opensabe.base.vo.IntValueEnum;
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

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * 通过 {@link IntValueEnum#getValue()} 将 {@link RequestParam}、{@link RequestHeader}
 * 中的int值转换为枚举值
 * @author heng.ma
 */
@ControllerAdvice
public class EnumConvertConfiguration {


    @InitBinder
    public void initBinder (WebDataBinder dataBinder) {
        ConversionService service = dataBinder.getConversionService();
        if (service instanceof ConverterRegistry registry) {
            registry.addConverter(new IntValueEnumConverter());
        }
    }




    public static class IntValueEnumConverter implements ConditionalGenericConverter {
        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            return IntValueEnum.class.isAssignableFrom(targetType.getType());
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Set.of(new ConvertiblePair(String.class, IntValueEnum.class), new ConvertiblePair(Integer.class, IntValueEnum.class));
        }


        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (isInteger(source.toString())) {
                IntValueEnum[] enums = (IntValueEnum[]) IntValueEnum.values((Class) targetType.getType());
                try {
                    return Arrays.stream(enums)
                            .filter(e -> Objects.equals(Integer.valueOf(source.toString()), e.getValue()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "No enum constant " + targetType.getType().getCanonicalName() + "." + source));
                }catch (NumberFormatException e) {
                    return Enum.valueOf((Class) targetType.getType(), source.toString());
                }
            }
            return Enum.valueOf((Class) targetType.getType(), source.toString());
        }
    }

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
}
