package io.github.opensabe.common.s3.test;

import io.github.opensabe.common.s3.test.common.S3BaseTest;
import io.github.opensabe.common.s3.typehandler.S3JsonConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AbstractPersistentProperty;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

import java.lang.annotation.Annotation;

/**
 * @author heng.ma
 */
@Slf4j
public class ConverterTest extends S3BaseTest {

    @Autowired
    private S3JsonConverter converter;

    @Test
    void testRead () throws NoSuchFieldException {
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

    public record Child (String id, Integer age){}
}
