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
package io.github.opensabe.base.vo;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 所有枚举带值的父类，枚举实现该接口，可以自动生成swagger文档
 *
 * @author heng.ma
 */
public interface IntValueEnum {

    /** 枚举常量数组缓存，避免重复反射 {@link Class#getEnumConstants()}。 */
    Map<Class<? extends Enum<?>>, Object[]> VALUES = new ConcurrentHashMap<>();

    /**
     * 获取枚举所有值
     *
     * @param enumClass 枚举类型对应的Class
     * @param <E>       枚举类型
     * @return enumClass对应的枚举数组
     */
    @SuppressWarnings("unchecked")
    static <E extends Enum<E>> E[] values(Class<E> enumClass) {
        E[] objects = (E[]) VALUES.get(enumClass);
        if (objects == null) {
            objects = enumClass.getEnumConstants();
            VALUES.put(enumClass, objects);
        }

        return objects;
    }

    /**
     * 按整型值解析枚举常量。
     *
     * @param enumClass 枚举类型
     * @param value     整型值
     * @param <E>       枚举类型
     * @return 匹配的枚举常量
     * @throws IllegalArgumentException 无匹配值时
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <E extends IntValueEnum> E of(Class<E> enumClass, int value) {
        E[] values = (E[]) IntValueEnum.values((Class) enumClass);
        for (E type : values) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant " + enumClass.getCanonicalName() + " for value " + value);
    }

    /**
     * 枚举对应的整型值，JSON 序列化时使用该值。
     *
     * @return 整型值
     */
    @JsonValue
    Integer getValue();

}
