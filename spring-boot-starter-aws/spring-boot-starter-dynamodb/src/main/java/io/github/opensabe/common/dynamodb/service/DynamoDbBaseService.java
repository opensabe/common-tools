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
package io.github.opensabe.common.dynamodb.service;

import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.data.core.TypeInformation;

import cn.hutool.core.bean.BeanDesc;
import cn.hutool.core.bean.BeanUtil;
import io.github.opensabe.common.dynamodb.annotation.HashKeyName;
import io.github.opensabe.common.dynamodb.annotation.RangeKeyName;
import io.github.opensabe.common.dynamodb.annotation.TableName;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableMetadata;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.internal.mapper.BeanAttributeGetter;
import software.amazon.awssdk.enhanced.dynamodb.internal.mapper.BeanAttributeSetter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * DynamoDB Enhanced 表操作基类。
 * <p>
 * 从子类泛型参数推导实体类型，优先 {@link TableSchema#fromClass(Class)}；
 * 失败时根据 {@link HashKeyName}/{@link RangeKeyName} 注解构建 {@link StaticTableSchema}。
 *
 * @param <T> 表实体类型
 */
@Log4j2
public abstract class DynamoDbBaseService<T> {

    /** 绑定实体类型的 Enhanced 表句柄。 */
    protected final DynamoDbTable<T> table;

    /**
     * 解析子类泛型 {@code T}，解析 {@link TableName} 并构建 {@link DynamoDbTable}。
     *
     * @param environment 用于解析表名占位符
     * @param client Enhanced 客户端
     */
    protected DynamoDbBaseService(Environment environment, DynamoDbEnhancedClient client) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) Objects.requireNonNull(TypeInformation.of(this.getClass()).getSuperTypeInformation(DynamoDbBaseService.class))
                .getTypeArguments().get(0).getType();
        this.table = client.table(environment.resolvePlaceholders(table(type)), tableSchema(type));
    }

    /**
     * 读取实体类上的 {@link TableName} 注解值。
     *
     * @param type 实体类
     * @return 物理表名
     * @throws IllegalStateException 未标注表名
     */
    protected String table(Class<T> type) {
        TableName tableName = AnnotatedElementUtils.findMergedAnnotation(type, TableName.class);
        if (Objects.nonNull(tableName)) {
            return tableName.name();
        }
        throw new IllegalStateException("No @TableName on entity: " + this.getClass().getName());
    }

    /**
     * 构建表 Schema；标准 Bean 映射失败时回退到注解驱动的静态 Schema。
     *
     * @param type 实体类
     * @return 表 Schema
     */
    protected TableSchema<T> tableSchema(Class<T> type) {
        try {
            return TableSchema.fromClass(type);
        } catch (Throwable e) {
            return customerSchema(type);
        }
    }

    /**
     * 按主键获取单条记录。
     * <p>
     * 查询条件应仅包含 {@link HashKeyName}/{@link RangeKeyName} 标识的字段，其它字段不参与键匹配。
     *
     * @param item 含主键（及可选排序键）的实体
     * @return 匹配项，不存在时为 {@code null}
     */
    public T selectOne(T item) {
        return table.getItem(item);
    }

    /**
     * 按键或排序键条件列出记录。
     * <p>
     * 若 item 无法构成完整 partition key，则对 sort key 字段执行 scan 过滤（不支持仅 sort key 的 query）。
     *
     * @param item 查询条件实体
     * @return 匹配记录列表
     */
    public List<T> selectList(T item) {
        Key key;
        try {
            key = table.keyFrom(item);
        } catch (IllegalArgumentException e) {
            TableSchema<T> schema = table.tableSchema();
            String sortKey = schema.tableMetadata().indexSortKey(TableMetadata.primaryIndexName()).orElseThrow();
            AttributeValue value = schema.attributeValue(item, sortKey);
            return table.scan(b -> b.filterExpression(Expression.builder()
                    .expression("#sortKey = :sortKey")
                    .putExpressionName("#sortKey", sortKey)
                    .putExpressionValue(":sortKey", value).build())).items().stream().toList();
        }
        return table.query(QueryConditional.keyEqualTo(key)).items().stream().toList();
    }

    /**
     * 使用显式 {@link QueryConditional} 查询。
     *
     * @param conditional 查询条件
     * @return 结果列表
     */
    public List<T> selectList(QueryConditional conditional) {
        return table.query(conditional).items().stream().toList();
    }

    /**
     * 写入或覆盖整条记录。
     *
     * @param item 实体
     */
    public void save(T item) {
        table.putItem(item);
    }

    /**
     * 按主键删除记录。
     *
     * @param item 含主键的实体
     */
    public void deleteByKey(T item) {
        table.deleteItem(table.keyFrom(item));
    }

    /**
     * 基于 Hutool {@link BeanDesc} 与注解构建 {@link StaticTableSchema}。
     *
     * @param type 实体类
     * @return 静态表 Schema
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private TableSchema<T> customerSchema(Class<T> type) {
        BeanDesc desc = BeanUtil.getBeanDesc(type);

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        StaticAttribute[] attributes = desc.getProps().stream().map(prop -> {
            StaticAttribute.Builder<T, ?> builder = StaticAttribute.builder(type, prop.getFieldClass())
                    .getter(BeanAttributeGetter.create(type, prop.getGetter(), lookup))
                    .setter(BeanAttributeSetter.create(type, prop.getSetter(), lookup));
            HashKeyName hashKey = AnnotatedElementUtils.findMergedAnnotation(prop.getField(), HashKeyName.class);
            RangeKeyName rangeKey = AnnotatedElementUtils.findMergedAnnotation(prop.getField(), RangeKeyName.class);
            if (Objects.nonNull(hashKey)) {
                String name = hashKey.name();
                if (StringUtils.isBlank(name)) {
                    name = prop.getFieldName();
                }
                builder.name(name).addTag(StaticAttributeTags.primaryPartitionKey());
            } else if (Objects.nonNull(rangeKey)) {
                String name = rangeKey.name();
                if (StringUtils.isBlank(name)) {
                    name = prop.getFieldName();
                }
                builder.name(name).addTag(StaticAttributeTags.primarySortKey());
            } else {
                builder.name(prop.getFieldName());
            }
            if (Date.class.isAssignableFrom(prop.getFieldClass())) {
                builder.attributeConverter((AttributeConverter) new DateAttributeConverter());
            }
            return builder.build();
        }).toArray(StaticAttribute[]::new);
        return StaticTableSchema.builder(type).newItemSupplier(() -> BeanUtils.instantiateClass(type)).attributes(attributes).build();
    }

    /**
     * {@link Date} 与 ISO-8601 UTC 字符串（{@code yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}）互转的 Enhanced 转换器。
     */
    public static class DateAttributeConverter implements AttributeConverter<Date> {

        /** {@inheritDoc} */
        @Override
        public AttributeValue transformFrom(Date input) {
            return AttributeValue.fromS(DateFormatUtils.format(input, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        }

        /** {@inheritDoc} */
        @Override
        public Date transformTo(AttributeValue input) {
            try {
                return DateUtils.parseDate(input.s(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            } catch (ParseException e) {
                throw new RuntimeException("Failed to parse DynamoDB date attribute: " + input.s(), e);
            }
        }

        /** {@inheritDoc} */
        @Override
        public EnhancedType<Date> type() {
            return EnhancedType.of(Date.class);
        }

        /** {@inheritDoc} */
        @Override
        public AttributeValueType attributeValueType() {
            return AttributeValueType.S;
        }
    }

}
