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
package io.github.opensabe.common.bytecode;

import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *  byte buddy 实现属性复制，修改字节码，动态创建类，该类复制属性通过直接写get set方法实现
 * @param <S>   源对象类型
 * @param <T>   目标对象类型
 * @author maheng
 */
public abstract class ByteBuddyBeanCopier<S, T> implements BeanCopier<S, T>{

    //source跟target属性对应关系，复制属性时，都是这些属性
    private final List<PropertyTransformer> propertyTransformers;

    protected ByteBuddyBeanCopier(Class<S> source, Class<T> target) {
        Map<String, PropertyDescriptor> getters = Arrays.stream(BeanUtils.getPropertyDescriptors(source))
                .filter(p -> Objects.nonNull(p.getReadMethod()))
                .collect(Collectors.toMap(PropertyDescriptor::getName, p -> p));
        this.propertyTransformers = Arrays.stream(BeanUtils.getPropertyDescriptors(target))
                .filter(p -> Objects.nonNull(p.getWriteMethod()))
                .filter(p -> getters.containsKey(p.getName()))
                .filter(p -> isAssignable(getters.get(p.getName()), p))
                .map(p -> new PropertyTransformer(getters.get(p.getName()), p))
                .toList();
    }

    /**
     * 创建BeanCopier入口方法
     * @param source    源对象类型
     * @param target    目标对象类型
     * @return  BeanCopier
     * @param <S>       源对象类型
     * @param <T>       目标对象类型
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <S, T> ByteBuddyBeanCopier<S, T> create (Class<S> source, Class<T> target) {
        try (var unloaded = new ByteBuddy(ClassFileVersion.ofThisVm())
                .subclass(ByteBuddyBeanCopier.class)
                .method(ElementMatchers.named("copy").and(ElementMatchers.isAbstract()))
                .intercept(MethodDelegation.to(new Interceptor<>()))
                .make()){
            var service = unloaded.load(ByteBuddyBeanCopier.class.getClassLoader()).getLoaded();
            return ReflectionUtils.accessibleConstructor(service, Class.class, Class.class).newInstance(source, target);
        }

    }


    private static boolean isAssignable (PropertyDescriptor source, PropertyDescriptor target) {
        Method readMethod = source.getReadMethod();
        Method writeMethod = target.getWriteMethod();

        if (writeMethod == null || readMethod == null) {
            return false;
        }

        ResolvableType sourceResolvableType = ResolvableType.forMethodReturnType(readMethod);
        ResolvableType targetResolvableType = ResolvableType.forMethodParameter(writeMethod, 0);

        Type paramType = writeMethod.getGenericParameterTypes()[0];
        if (paramType instanceof Class<?> clazz) {
            return ClassUtils.isAssignable(clazz, readMethod.getReturnType());
        }
        else if (paramType.equals(readMethod.getGenericReturnType())) {
            return true;
        }
        // Ignore generic types in assignable check if either ResolvableType has unresolvable generics.
       return
                (sourceResolvableType.hasUnresolvableGenerics() || targetResolvableType.hasUnresolvableGenerics() ?
                        ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType()) :
                        targetResolvableType.isAssignableFrom(sourceResolvableType));
    }


    public static class Interceptor<S, T> {


        @SuppressWarnings("unused")
        public void doCopy (@Argument(0) S source, @Argument(1) T target, @This ByteBuddyBeanCopier<S, T> thi) {
            thi.propertyTransformers.forEach(t -> t.invoke(source, target));
        }

    }

    class PropertyTransformer  {

        private final Method getter;

        private final Method setter;

        PropertyTransformer(Method getter, Method setter) {
            this.getter = getter;
            this.setter = setter;
        }

        PropertyTransformer(PropertyDescriptor getter, PropertyDescriptor setter) {
            this(getter.getReadMethod(), setter.getWriteMethod());
        }

        @SneakyThrows
        void invoke (S source, T target) {
            setter.invoke(target, getter.invoke(source));
        }
    }
}
