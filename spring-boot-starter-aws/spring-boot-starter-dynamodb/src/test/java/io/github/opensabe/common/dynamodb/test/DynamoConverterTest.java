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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AbstractPersistentProperty;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

import io.github.opensabe.common.dynamodb.test.common.DynamicdbStarter;
import io.github.opensabe.common.dynamodb.typehandler.DynamodbConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author heng.ma
 */
public class DynamoConverterTest extends DynamicdbStarter {

    @Autowired
    private DynamodbConverter converter;

    @Test
    void testConvert() throws NoSuchFieldException {
        BasicPersistentEntity entity = new BasicPersistentEntity<>(TypeInformation.of(Entity.class));
        entity.addPersistentProperty(new AbstractPersistentProperty(Property.of(TypeInformation.of(Entity.class), Entity.class.getDeclaredField("child")), entity, SimpleTypeHolder.DEFAULT) {
            @Override
            public boolean isIdProperty() {
                return false;
            }

            @Override
            public boolean isVersionProperty() {
                return false;
            }

            @Override
            public boolean isAnnotationPresent(Class annotationType) {
                return false;
            }

            @Override
            public Annotation findPropertyOrOwnerAnnotation(Class annotationType) {
                return null;
            }

            @Override
            public Annotation findAnnotation(Class annotationType) {
                return null;
            }

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
        private String name;

        private Integer age;

        private Child child;
    }

    public record Child(String id, Integer age) {
    }
}
