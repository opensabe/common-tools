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
package io.github.opensabe.common.entity.base.vo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.springframework.data.util.TypeInformation;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 消息反序列化帮助类
 *
 * @author heng.ma
 */
public class MessageTypeReference<T> extends TypeReference<T> {

    private final Type type;

    private final Type baseMessageType;

    private MessageTypeReference(final TypeInformation<T> information) {
        final List<TypeInformation<?>> arguments = information.getTypeArguments();
        this.type = new ParameterizedType() {
            public Type[] getActualTypeArguments() {
                return arguments.stream().map(i -> i.toTypeDescriptor().getResolvableType().getType()).toArray(Type[]::new);
            }

            public Type getRawType() {
                return information.getType();
            }

            public Type getOwnerType() {
                return null;
            }
        };
        if (!TypeInformation.of(BaseMessage.class).isAssignableFrom(information)) {
            this.baseMessageType = new ParameterizedType() {
                @Override
                public Type[] getActualTypeArguments() {
                    return new Type[]{information.toTypeDescriptor().getResolvableType().getType()};
                }

                @Override
                public Type getRawType() {
                    return BaseMessage.class;
                }

                @Override
                public Type getOwnerType() {
                    return null;
                }
            };
        } else {
            this.baseMessageType = null;
        }
    }

    public static <T> MessageTypeReference<T> fromTypeInformation(TypeInformation<T> information) {
        return new MessageTypeReference<>(information);
    }

    @Override
    public Type getType() {
        return type;
    }


    @SuppressWarnings("unchecked")
    public TypeReference<BaseMessage<T>> baseMessageType() {
        if (baseMessageType == null) {
            return (TypeReference<BaseMessage<T>>) this;
        }
        return new TypeReference<>() {
            @Override
            public Type getType() {
                return baseMessageType;
            }
        };
    }
}
