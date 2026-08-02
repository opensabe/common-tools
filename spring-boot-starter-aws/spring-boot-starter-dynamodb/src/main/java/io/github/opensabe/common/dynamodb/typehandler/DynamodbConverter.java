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
package io.github.opensabe.common.dynamodb.typehandler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import org.springframework.core.env.Environment;
import org.springframework.data.convert.PropertyValueConverter;
import org.springframework.data.convert.ValueConversionContext;
import org.springframework.data.core.TypeInformation;

import tools.jackson.core.type.TypeReference;

import cn.hutool.core.codec.Hashids;
import io.github.opensabe.common.dynamodb.service.DynamoDbBaseService;
import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * 将复杂属性序列化至 DynamoDB 转换表，持久化层仅保存 Hashids 主键。
 * <p>
 * 表名模板 {@code ${aws_env}_converter}；读写经 {@link JsonUtil}（epoch-ms 时间线格式）。
 *
 * @author heng.ma
 */
public class DynamodbConverter extends DynamoDbBaseService<DynamodbConverter.ConverterBean> implements PropertyValueConverter<Object, String, ValueConversionContext<?>> {

    /** 生成短主键的 Hashids 实例。 */
    private final Hashids hashids;

    /**
     * @param environment Spring 环境（解析表名占位符）
     * @param dynamoDbEnhancedClient Enhanced 客户端
     */
    public DynamodbConverter(Environment environment, DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        super(environment, dynamoDbEnhancedClient);
        this.hashids = Hashids.create("wdsfdgf3".toCharArray());
    }

    /** {@inheritDoc} — 返回 {@code ${aws_env}_converter}。 */
    @Override
    protected String table(Class<ConverterBean> type) {
        return "${aws_env}_converter";
    }

    /**
     * 按主键读取 JSON 并反序列化为属性声明类型。
     *
     * @param value 存储的主键 ID
     * @param context 属性转换上下文
     * @return 反序列化对象，无记录时为 {@code null}
     */
    @Override
    public Object read(String value, ValueConversionContext context) {
        ConverterBean bean = table.getItem(Key.builder().partitionValue(value).build());
        if (Objects.isNull(bean)) {
            return null;
        }
        return JsonUtil.parseObject(bean.getValue(), JacksonParameterizedTypeTypeReference.fromTypeInformation(context.getProperty().getTypeInformation()));
    }

    /**
     * 将对象 JSON 写入转换表并返回生成的主键。
     *
     * @param value 待持久化对象
     * @param context 属性转换上下文
     * @return Hashids 编码的主键
     */
    @Override
    public String write(Object value, ValueConversionContext context) {
        String id = hashids.encode(System.nanoTime(), Thread.currentThread().threadId());
        table.putItem(new ConverterBean(id, JsonUtil.toJSONString(value)));
        return id;
    }

    /**
     * DynamoDB 转换表行映射。
     */
    @Getter
    @DynamoDbBean
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConverterBean {

        /** 分区键（Hashids ID）。 */
        private String id;

        /** JSON 序列化后的属性值。 */
        private String value;

        /**
         * 设置分区键。
         *
         * @param id 主键
         */
        @DynamoDbPartitionKey
        public void setId(String id) {
            this.id = id;
        }

        /**
         * 设置 JSON 载荷。
         *
         * @param value JSON 字符串
         */
        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * 从 Spring {@link TypeInformation} 构造 Jackson {@link TypeReference}。
     *
     * @param <T> 目标类型
     */
    private static class JacksonParameterizedTypeTypeReference<T> extends TypeReference<T> {

        /** 参数化目标类型。 */
        private final ParameterizedType type;

        /**
         * @param information 属性类型信息
         */
        JacksonParameterizedTypeTypeReference(final TypeInformation<T> information) {
            final List<TypeInformation<?>> arguments = information.getTypeArguments();
            this.type = new ParameterizedType() {
                public Type[] getActualTypeArguments() {
                    return arguments.stream().map(TypeInformation::getType).toArray(Type[]::new);
                }

                public Type getRawType() {
                    return information.getType();
                }

                public Type getOwnerType() {
                    return null;
                }
            };
        }

        /**
         * 工厂方法。
         *
         * @param typeInformation 类型信息
         * @param <T> 目标类型
         * @return 类型引用
         */
        public static <T> JacksonParameterizedTypeTypeReference<T> fromTypeInformation(TypeInformation<T> typeInformation) {
            return new JacksonParameterizedTypeTypeReference<>(typeInformation);
        }

        /** {@inheritDoc} */
        public Type getType() {
            return this.type;
        }
    }

}
