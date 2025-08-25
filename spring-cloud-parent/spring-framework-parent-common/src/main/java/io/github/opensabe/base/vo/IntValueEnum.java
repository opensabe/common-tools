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

    @JsonValue
    Integer getValue();

}
