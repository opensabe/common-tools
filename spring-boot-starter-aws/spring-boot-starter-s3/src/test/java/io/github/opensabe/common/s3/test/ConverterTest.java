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
package io.github.opensabe.common.s3.test;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AbstractPersistentProperty;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

import io.github.opensabe.common.s3.test.common.S3BaseTest;
import io.github.opensabe.common.s3.typehandler.S3JsonConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author heng.ma
 */
@Slf4j
@DisplayName("S3转换器测试")
public class ConverterTest extends S3BaseTest {

    @Autowired
    private S3JsonConverter converter;

    @Test
    @DisplayName("测试S3 JSON转换器读写功能 - 验证对象序列化和反序列化")
    void testRead() throws NoSuchFieldException {
        BasicPersistentEntity entity = new BasicPersistentEntity<>(TypeInformation.of(MyEntity.class));
        entity.addPersistentProperty(new AbstractPersistentProperty(Property.of(TypeInformation.of(MyEntity.class), MyEntity.class.getDeclaredField("child")), entity, SimpleTypeHolder.DEFAULT) {
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
        Child value = new Child("child1", 20);
        String key = converter.write(value, () -> entity.getPersistentProperty("child"));
        log.info(key);

        Child child = (Child) converter.read(key, () -> entity.getPersistentProperty("child"));

        log.info(child.toString());

        Assertions.assertEquals(child, value);

    }


    @Getter
    @Setter
    public static class MyEntity {

        private String id;

        private String name;

        private Child child;
    }

    public record Child(String id, Integer age) {
    }
}
