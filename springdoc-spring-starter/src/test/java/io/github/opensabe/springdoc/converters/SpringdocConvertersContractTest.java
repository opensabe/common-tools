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

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import io.github.opensabe.base.vo.IntValueEnum;
import io.github.opensabe.springdoc.responses.page.PageModelConverter;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Upgrade gate: springdoc Page / IntValueEnum schema contracts.
 */
@DisplayName("springdoc converters 升级契约")
class SpringdocConvertersContractTest {

    @Test
    @DisplayName("Page 类型 introspection 仅暴露 list 与 total")
    void pageModelConverterKeepsListAndTotal() {
        PageModelConverter.config();
        ObjectMapper mapper = Json.mapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType pageType = mapper.getTypeFactory().constructParametricType(Page.class, String.class);
        BeanDescription description = config.introspect(pageType);
        List<String> names = description.findProperties().stream()
                .map(BeanPropertyDefinition::getName)
                .toList();
        assertEquals(2, names.size());
        assertTrue(names.contains("list"));
        assertTrue(names.contains("total"));
    }

    @Test
    @DisplayName("IntValueEnum schema 为 int，description 含 value-name，enum 为取值列表")
    void enumModelConverterBuildsIntSchema() {
        EnumModelConverter converter = new EnumModelConverter();
        @SuppressWarnings("rawtypes")
        Schema schema = converter.resolve(
                new AnnotatedType(SampleType.class),
                mock(ModelConverterContext.class),
                emptyChain()
        );
        assertNotNull(schema);
        assertEquals("integer", schema.getType());
        assertTrue(schema.getDescription().contains("1-A"));
        assertTrue(schema.getDescription().contains("2-B"));
        assertEquals(List.of(1, 2), schema.getEnum());
    }

    @Test
    @DisplayName("ParameterCustomizer 将 schema description 合并到 parameter")
    void parameterCustomizerMergesDescription() throws NoSuchMethodException {
        EnumModelConverter converter = new EnumModelConverter();
        Parameter parameter = new Parameter();
        Schema<Object> schema = new Schema<>();
        schema.setDescription("1-A,2-B");
        parameter.setSchema(schema);
        parameter.setDescription("status");

        MethodParameter methodParameter = new MethodParameter(
                DummyController.class.getDeclaredMethod("byType", SampleType.class), 0);

        Parameter customized = converter.customize(parameter, methodParameter);
        assertEquals("status(1-A,2-B)", customized.getDescription());
    }

    private static Iterator<ModelConverter> emptyChain() {
        return List.<ModelConverter>of().iterator();
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

    static class DummyController {
        @SuppressWarnings("unused")
        public void byType(SampleType type) {
        }
    }
}
