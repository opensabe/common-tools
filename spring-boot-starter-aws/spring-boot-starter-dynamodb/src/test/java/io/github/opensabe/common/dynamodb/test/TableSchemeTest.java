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

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.TypeInformation;

import cn.hutool.core.bean.BeanDesc;
import cn.hutool.core.bean.BeanUtil;
import io.github.opensabe.common.dynamodb.annotation.HashKeyName;
import io.github.opensabe.common.dynamodb.annotation.RangeKeyName;
import io.github.opensabe.common.dynamodb.service.DynamoDbBaseService;
import io.github.opensabe.common.dynamodb.service.KeyValueDynamoDbService;
import io.github.opensabe.common.dynamodb.test.po.EightDataTypesPo;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.TableMetadata;
import software.amazon.awssdk.enhanced.dynamodb.internal.mapper.BeanAttributeGetter;
import software.amazon.awssdk.enhanced.dynamodb.internal.mapper.BeanAttributeSetter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

/**
 * @author heng.ma
 */
public class TableSchemeTest {


    @Test
    void testParamerizedType() {
        TypeInformation<KeyValueDynamoDbService> information = TypeInformation.of(KeyValueDynamoDbService.class);
        TypeInformation<?> superTypeInformation = information.getSuperTypeInformation(DynamoDbBaseService.class);
        Assertions.assertEquals(KeyValueDynamoDbService.KeyValueMap.class, superTypeInformation.getTypeArguments().get(0).getType());
    }

    @Test
    void testGenerateTableScheme() {
        BeanDesc desc = BeanUtil.getBeanDesc(EightDataTypesPo.class);
        StaticAttribute[] attributes = desc.getProps().stream().map(prop -> {
            StaticAttribute.Builder<EightDataTypesPo, ?> builder = StaticAttribute.builder(EightDataTypesPo.class, prop.getFieldClass())
                    .setter(BeanAttributeSetter.create(EightDataTypesPo.class, prop.getSetter()))
                    .getter(BeanAttributeGetter.create(EightDataTypesPo.class, prop.getGetter()));
            HashKeyName keyName = prop.getField().getAnnotation(HashKeyName.class);
            if (Objects.nonNull(keyName)) {
                builder = builder.name("".equals(keyName.name()) ? prop.getFieldName() : keyName.name())
                        .addTag(StaticAttributeTags.primaryPartitionKey());
            } else {
                builder.name(prop.getFieldName());
            }
            RangeKeyName rangeKey = prop.getField().getAnnotation(RangeKeyName.class);
            if (Objects.nonNull(rangeKey)) {
                builder = builder.addTag(StaticAttributeTags.primarySortKey());
            }
            if (Date.class.isAssignableFrom(prop.getFieldClass())) {
                builder.attributeConverter((AttributeConverter) new DynamoDbBaseService.DateAttributeConverter());
            }
            return builder.build();
        }).toArray(StaticAttribute[]::new);

        StaticTableSchema schema = StaticTableSchema.builder(EightDataTypesPo.class).attributes(attributes).build();

        System.out.println(schema.attributeNames());

        Assertions.assertEquals(desc.getProps().size(), schema.attributeNames().size());

        Optional<String> optional = schema.tableMetadata().indexSortKey(TableMetadata.primaryIndexName());
        System.out.println(optional);

        org.assertj.core.api.Assertions.assertThat(optional)
                .isPresent()
                .get()
                .isEqualTo("order");

        String id = schema.tableMetadata().indexPartitionKey(TableMetadata.primaryIndexName());

        Assertions.assertEquals("id", id);

    }

}