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
package io.github.opensabe.common.dynamodb.test;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AbstractPersistentProperty;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.core.TypeInformation;

import io.github.opensabe.common.dynamodb.test.common.DynamicdbStarter;
import io.github.opensabe.common.dynamodb.typehandler.DynamodbConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DynamoDB 属性转换器读写测试。
 */
@DisplayName("DynamoDB属性转换器测试")
public class DynamoConverterTest extends DynamicdbStarter {

    /** converter。 */
    @Autowired
    private DynamodbConverter converter;

    /**
     * 嵌套 record 属性应能序列化后再反序列化为等价对象。
     */
    @DisplayName("验证isIdProperty")
    @Test
    void testConvert() throws NoSuchFieldException {
        BasicPersistentEntity entity = new BasicPersistentEntity<>(TypeInformation.of(Entity.class));
        entity.addPersistentProperty(new AbstractPersistentProperty(Property.of(TypeInformation.of(Entity.class), Entity.class.getDeclaredField("child")), entity, SimpleTypeHolder.DEFAULT) {
            /** {@inheritDoc} */
            @Override
            public boolean isIdProperty() {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public boolean isVersionProperty() {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public boolean isAnnotationPresent(Class annotationType) {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public Annotation findPropertyOrOwnerAnnotation(Class annotationType) {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public Annotation findAnnotation(Class annotationType) {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            protected Association createAssociation() {
                return null;
            }
        });
        String key = converter.write(new Child("sdfda", 20), () -> entity.getPersistentProperty("child"));
        System.out.println(key);
        Object child = converter.read(key, () -> entity.getPersistentProperty("child"));
        System.out.println(child);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entity {
/** name。 */
        private String name;

/** age。 */
        private Integer age;

/** child。 */
        private Child child;
    }

    public record Child(String id, Integer age) {
    }
}
