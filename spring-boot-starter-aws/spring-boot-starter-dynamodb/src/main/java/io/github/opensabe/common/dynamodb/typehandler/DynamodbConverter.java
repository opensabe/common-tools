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

import cn.hutool.core.codec.Hashids;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.opensabe.common.dynamodb.service.DynamoDbBaseService;
import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.convert.PropertyValueConverter;
import org.springframework.data.convert.ValueConversionContext;
import org.springframework.data.util.TypeInformation;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * @author heng.ma
 */

public class DynamodbConverter extends DynamoDbBaseService<DynamodbConverter.ConverterBean> implements PropertyValueConverter<Object, String, ValueConversionContext<?>> {

    private final Hashids hashids;
    public DynamodbConverter(Environment environment, DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        super(environment, dynamoDbEnhancedClient);
        this.hashids = Hashids.create("wdsfdgf3".toCharArray());
    }

    @Override
    protected String table(Class<ConverterBean> type) {
        return "${aws_env}_converter";
    }

    @Override
    public Object read(String value, ValueConversionContext context) {
        ConverterBean bean = table.getItem(Key.builder().partitionValue(value).build());
        if (Objects.isNull(bean)) {
            return null;
        }
        return JsonUtil.parseObject(bean.getValue(), JacksonParameterizedTypeTypeReference.fromTypeInformation(context.getProperty().getTypeInformation()));
    }

    @Override
    public String write(Object value, ValueConversionContext context) {
        String id = hashids.encode(System.nanoTime(), Thread.currentThread().getId());
        table.putItem(new ConverterBean(id, JsonUtil.toJSONString(value)));
        return id;
    }

    @Getter
    @DynamoDbBean
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConverterBean  {

        private  String id;

        private String value;

        @DynamoDbPartitionKey
        public void setId(String id) {
            this.id = id;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


    private static class JacksonParameterizedTypeTypeReference<T> extends TypeReference<T> {
        private final ParameterizedType type;

        public static <T> JacksonParameterizedTypeTypeReference<T> fromTypeInformation(TypeInformation<T> typeInformation) {
            return new JacksonParameterizedTypeTypeReference<>(typeInformation);
        }

        JacksonParameterizedTypeTypeReference(final TypeInformation<T> information) {
            final List<TypeInformation<?>> arguments = information.getTypeArguments();
            this.type = new ParameterizedType() {
                public Type [] getActualTypeArguments() {
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

        public Type getType() {
            return this.type;
        }
    }

}
