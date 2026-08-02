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
 * Bean 属性复制与 MapStruct 转换工具。
 * <p>
 * 高频复制场景使用 ByteBuddy {@link BeanCopier} 缓存，避免 Spring {@code BeanUtils} 在 5.3+ 的内存开销；
 * 类型转换走 MapStruct {@link io.github.opensabe.mapstruct.core.MapperRepository}。
 */
public class BeanUtils {
    private static final Cache<String, BeanCopier<?, ?>> CACHE = Caffeine.newBuilder().build();
    private static final MapperRepository MAPPER_REPOSITORY = MapperRepository.getInstance();

    /**
     * 将 source 属性复制到 target（同名字段）。
     *
     * @param source 源对象
     * @param target 目标对象
     */
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
     * 将 source 映射为 target 类型的新实例。
     *
     * @param source 源对象
     * @param target 目标类型
     * @param <S>    源类型
     * @param <T>    目标类型
     * @return 映射后的新实例
     * @throws io.github.opensabe.mapstruct.core.MapperNotFoundException 源或目标类未标注 {@link io.github.opensabe.mapstruct.core.Binding}
     */
    @SuppressWarnings("unchecked")
    public static <S, T> T transform(S source, Class<T> target) {
        return MAPPER_REPOSITORY.getMapper((Class<S>) source.getClass(), target).map(source);
    }

    /**
     * 由 Map 构造 target 类型实例（浅拷贝，不解析嵌套 Map）。
     *
     * @param map    字段名到值的映射
     * @param target 目标类型
     * @param <T>    目标类型
     * @return 构造的实例
     * @throws io.github.opensabe.mapstruct.core.MapperNotFoundException 目标类未标注 {@link io.github.opensabe.mapstruct.core.Binding}
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> target) {
        return MAPPER_REPOSITORY.getMapMapper(target).fromMap(map);
    }

}
