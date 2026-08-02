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

import org.springframework.data.core.TypeInformation;

import tools.jackson.core.type.TypeReference;

/**
 * 基于 Spring {@link TypeInformation} 的 Jackson {@link TypeReference} 工厂。
 * <p>
 * 供 RocketMQ 消费者从泛型参数推导消息体类型；若目标类型非 {@link BaseMessage} 子类，
 * 额外提供 {@link #baseMessageType()} 以解析完整信封。
 *
 * @param <T> 消息体或信封类型
 * @author heng.ma
 */
public class MessageTypeReference<T> extends TypeReference<T> {

    /** 用于 Jackson 解析消息体 {@code T} 的参数化类型。 */
    private final Type type;

    /** 当 {@code T} 非 {@link BaseMessage} 时，解析 {@code BaseMessage<T>} 的类型；否则为 {@code null}。 */
    private final Type baseMessageType;

    /**
     * 从类型信息构建引用；{@code T} 非 {@link BaseMessage} 时同步构造信封类型。
     *
     * @param information Spring 类型信息
     */
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

    /**
     * 工厂方法。
     *
     * @param information 类型信息
     * @param <T> 目标类型
     * @return 类型引用
     */
    public static <T> MessageTypeReference<T> fromTypeInformation(TypeInformation<T> information) {
        return new MessageTypeReference<>(information);
    }

    /** {@inheritDoc} */
    @Override
    public Type getType() {
        return type;
    }

    /**
     * 返回 {@code BaseMessage<T>} 的 Jackson 类型引用；若 {@code T} 已是 {@link BaseMessage} 则返回自身。
     *
     * @param <T> 消息体类型
     * @return 信封类型引用
     */
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
