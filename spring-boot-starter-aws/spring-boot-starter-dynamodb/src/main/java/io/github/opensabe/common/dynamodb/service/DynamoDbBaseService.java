package io.github.opensabe.common.dynamodb.service;

import cn.hutool.core.bean.BeanDesc;
import cn.hutool.core.bean.BeanUtil;
import io.github.opensabe.common.dynamodb.annotation.HashKeyName;
import io.github.opensabe.common.dynamodb.annotation.RangeKeyName;
import io.github.opensabe.common.dynamodb.annotation.TableName;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.data.util.TypeInformation;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.internal.mapper.BeanAttributeGetter;
import software.amazon.awssdk.enhanced.dynamodb.internal.mapper.BeanAttributeSetter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Log4j2
public abstract class DynamoDbBaseService<T> {

    protected final DynamoDbTable<T> table;

    protected DynamoDbBaseService(Environment environment, DynamoDbEnhancedClient client) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) Objects.requireNonNull(TypeInformation.of(this.getClass()).getSuperTypeInformation(DynamoDbBaseService.class))
                .getTypeArguments().get(0).getType();
        this.table = client.table(environment.resolvePlaceholders(table(type)), tableSchema(type));
    }

    protected String table (Class<T> type) {
        TableName tableName = AnnotatedElementUtils.findMergedAnnotation(type, TableName.class);
        if (Objects.nonNull(tableName)) {
            return tableName.name();
        }
        throw new IllegalStateException ("no table name assignment "+ this.getClass().getName());
    }

    protected TableSchema<T> tableSchema (Class<T> type) {
        try {
            return TableSchema.fromClass(type);
        }catch (Throwable e) {
            return customerSchema(type);
        }
    }

    /**
     * 有且只能包含用@HashKeyname或@RangekeyName标识字段值，其它字段查询不生效
     */
    public T selectOne(T item) {
        //暂不支持仅通过sort key查询
        return table.getItem(item);
    }

    /**
     * 有且只能包含用@HashKeyname或@RangekeyName标识字段值,其它字段查询不生效
     *
     */
    public List<T> selectList(T item) {
        Key key;
        try {
            key = table.keyFrom(item);
        } catch (IllegalArgumentException e) {
            //query查询必须包含partition key，所以只通过 sort key查询时得用scan
            TableSchema<T> schema = table.tableSchema();
            String _key = schema.tableMetadata().indexSortKey(TableMetadata.primaryIndexName()).orElseThrow();
            AttributeValue value = schema.attributeValue(item, _key);
            return table.scan(b -> b.filterExpression(Expression.builder()
                            .expression("#sortKey = :sortKey")
                            .putExpressionName("#sortKey", _key)
                            .putExpressionValue(":sortKey", value).build())).items().stream().toList();
        }
        return table.query(QueryConditional.keyEqualTo(key)).items().stream().toList();
    }

    public List<T> selectList(QueryConditional conditional) {
        return table.query(conditional).items().stream().toList();
    }

    public void save(T item) {
        table.putItem(item);
    }


    public void deleteByKey (T item) {
        table.deleteItem(table.keyFrom(item));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private TableSchema<T> customerSchema (Class<T> type) {
        BeanDesc desc = BeanUtil.getBeanDesc(type);

        StaticAttribute[] attributes = desc.getProps().stream().map(prop -> {
            StaticAttribute.Builder<T, ?> builder = StaticAttribute.builder(type, prop.getFieldClass())
                    .getter(BeanAttributeGetter.create(type, prop.getGetter()))
                    .setter(BeanAttributeSetter.create(type, prop.getSetter()));
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

    public static class DateAttributeConverter implements AttributeConverter<Date> {

        @Override
        public AttributeValue transformFrom(Date input) {
            return AttributeValue.fromS(DateFormatUtils.format(input, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        }

        @Override
        public Date transformTo(AttributeValue input) {
            try {
                return DateUtils.parseDate(input.s(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public EnhancedType<Date> type() {
            return EnhancedType.of(Date.class);
        }

        @Override
        public AttributeValueType attributeValueType() {
            return AttributeValueType.S;
        }
    }

}
