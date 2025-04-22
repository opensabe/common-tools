package io.github.opensabe.common.entity.base.vo;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.data.util.TypeInformation;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息反序列化帮助类
 * @author heng.ma
 */
public class MessageTypeReference<T> extends TypeReference<T> {

    private final ParameterizedType type;

    private final ParameterizedType baseMessageType;

    private MessageTypeReference(final TypeInformation<T> information) {
        final List<TypeInformation<?>> arguments = information.getTypeArguments();
        this.type = new ParameterizedType() {
            public Type [] getActualTypeArguments() {
                return arguments.stream().map(TypeInformation::getType).toArray(Type[]::new);
            }

            public  Type getRawType() {
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
                    return new Type[] {information.toTypeDescriptor().getResolvableType().getType()};
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
        }else {
            this.baseMessageType = null;
        }
    }

    public static <T> MessageTypeReference<T> fromTypeInformation (TypeInformation<T> information) {
        return new MessageTypeReference<>(information);
    }

    @Override
    public Type getType() {
        return type;
    }


    @SuppressWarnings("unchecked")
    public TypeReference<BaseMessage<T>> baseMessageType () {
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
