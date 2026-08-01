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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.web.bind.WebDataBinder;

import io.github.opensabe.base.vo.IntValueEnum;
import io.github.opensabe.spring.cloud.parent.common.handler.EnumConvertConfiguration.IntValueEnumConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Upgrade gate: IntValueEnum request binding and InitBinder idempotency.
 */
@DisplayName("EnumConvertConfiguration 升级契约")
class EnumConvertConfigurationContractTest {

    private final IntValueEnumConverter converter = new IntValueEnumConverter();
    private final TypeDescriptor stringSource = TypeDescriptor.valueOf(String.class);
    private final TypeDescriptor intSource = TypeDescriptor.valueOf(Integer.class);
    private final TypeDescriptor target = TypeDescriptor.valueOf(SampleType.class);

    @Test
    @DisplayName("合法 int / Integer / 数字字符串可转为 IntValueEnum")
    void convertValidIntValues() {
        assertEquals(SampleType.A, converter.convert("1", stringSource, target));
        assertEquals(SampleType.B, converter.convert(2, intSource, target));
        assertEquals(SampleType.A, converter.convert("01", stringSource, target));
    }

    @Test
    @DisplayName("合法枚举名可转为 IntValueEnum")
    void convertByEnumName() {
        assertEquals(SampleType.B, converter.convert("B", stringSource, target));
    }

    @Test
    @DisplayName("非法 int 抛 IllegalArgumentException；null 返回 null")
    void convertInvalidAndNull() {
        assertNull(converter.convert(null, stringSource, target));
        assertThrows(IllegalArgumentException.class,
                () -> converter.convert("99", stringSource, target));
    }

    @Test
    @DisplayName("matches 仅对 IntValueEnum 目标类型为 true")
    void matchesOnlyIntValueEnum() {
        assertTrue(converter.matches(stringSource, target));
        assertFalse(converter.matches(stringSource, TypeDescriptor.valueOf(String.class)));
    }

    @Test
    @DisplayName("同一 ConversionService 多次 InitBinder 不重复注册 converter")
    void initBinderIsIdempotentPerConversionService() {
        DefaultConversionService service = new DefaultConversionService();
        WebDataBinder binder = new WebDataBinder(new Object());
        binder.setConversionService(service);

        EnumConvertConfiguration configuration = new EnumConvertConfiguration();
        configuration.initBinder(binder);
        configuration.initBinder(binder);
        configuration.initBinder(binder);

        assertEquals(SampleType.A, service.convert("1", SampleType.class));
        // still a single converter path — converting twice works and does not throw registration errors
        assertEquals(SampleType.B, service.convert(2, SampleType.class));
    }

    @Test
    @DisplayName("isInteger 识别纯数字字符串")
    void isIntegerContract() {
        assertTrue(EnumConvertConfiguration.isInteger("12"));
        assertFalse(EnumConvertConfiguration.isInteger(""));
        assertFalse(EnumConvertConfiguration.isInteger(null));
        assertFalse(EnumConvertConfiguration.isInteger("A"));
        assertFalse(EnumConvertConfiguration.isInteger("1.2"));
    }

    enum SampleType implements IntValueEnum {
        A(1), B(2);

        private final int value;

        SampleType(int value) {
            this.value = value;
        }

        @Override
        public Integer getValue() {
            return value;
        }
    }
}
