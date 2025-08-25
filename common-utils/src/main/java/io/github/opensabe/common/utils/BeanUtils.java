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
package io.github.opensabe.common.utils;

import java.util.Map;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.opensabe.common.bytecode.BeanCopier;
import io.github.opensabe.common.bytecode.ByteBuddyBeanCopier;
import io.github.opensabe.mapstruct.core.MapperRepository;


/**
 * Spring BeanUtils cause huge memory consuming since 5.3.x
 * Therefore using BeanCopier for heavy and constant copy
 */
public class BeanUtils {
    private static final Cache<String, BeanCopier<?, ?>> CACHE = Caffeine.newBuilder().build();
    private static final MapperRepository mapperRepository = MapperRepository.getInstance();

    @SuppressWarnings({"unchecked, rawtypes"})
    public static void copyProperties(Object source, Object target) {
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();
//        BeanCopier beanCopier = CACHE.get(sourceClass.getName() + " to " + targetClass.getName(), k -> {
//            return BeanCopier.create(sourceClass, targetClass, false);
//        });
        BeanCopier beanCopier = CACHE.get(sourceClass.getName() + " to " + targetClass.getName(), k -> ByteBuddyBeanCopier.create(source.getClass(), target.getClass()));
        beanCopier.copy(source, target);
    }

    /**
     * transform source to target and return a new Object of target
     *
     * @param source source object
     * @param target Type of target
     * @param <S>    Type of source
     * @param <T>    Type of target
     * @return instance of target
     * @throws io.github.opensabe.mapstruct.core.MapperNotFoundException if source or target class not contains annotation of {@link io.github.opensabe.mapstruct.core.Binding}
     */
    @SuppressWarnings("unchecked")
    public static <S, T> T transform(S source, Class<T> target) {
        return mapperRepository.getMapper((Class<S>) source.getClass(), target).map(source);
    }

    /**
     * create object by map
     * <p>
     * key must marches of the target fields and the type of map value' type should same as field's type.
     * </p>
     * <b>no deep copy, so the map value(Map < String, Map < String, ?>) is not resolved.</b>
     *
     * @param map    the map contains the field of target
     * @param target target type
     * @param <T>    Type of target
     * @return instance of target
     * @throws io.github.opensabe.mapstruct.core.MapperNotFoundException if target class not contains annotation of {@link io.github.opensabe.mapstruct.core.Binding}
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> target) {
        return mapperRepository.getMapMapper(target).fromMap(map);
    }

}
