package io.github.opensabe.base.vo;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 所有枚举带值的父类，枚举实现该接口，可以自动生成swagger文档
 * @author heng.ma
 */
public interface IntValueEnum {

   Map<Class<? extends Enum<?>>, Object[]> VALUES = new ConcurrentHashMap<>();

    /**
     * 获取枚举所有值
     * @param enumClass 枚举类型对应的Class
     * @return          enumClass对应的枚举数组
     * @param <E>       枚举类型
     */
    @SuppressWarnings("unchecked")
    static <E extends Enum<E>> E[] values (Class<E> enumClass) {
        E[] objects = (E[])VALUES.get(enumClass);
        if (objects == null) {
            objects = enumClass.getEnumConstants();
            VALUES.put(enumClass, objects);
        }

        return objects;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <E extends IntValueEnum> E of (Class<E> enumClass, int value) {
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
