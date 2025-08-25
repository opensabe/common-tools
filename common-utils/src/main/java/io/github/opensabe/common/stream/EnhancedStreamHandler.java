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
package io.github.opensabe.common.stream;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnhancedStreamHandler<T> implements InvocationHandler {
    private Stream<T> delegate;

    public EnhancedStreamHandler(Stream<T> delegate) {
        this.delegate = delegate;
    }

    private static final Method ENHANCED_DISTINCT;
    static {
        try {
            ENHANCED_DISTINCT = EnhancedStream.class.getMethod(
                    "distinct", ToIntFunction.class, BiPredicate.class,
                    BinaryOperator.class
            );
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }

    /**
     * 将EnhancedStream的方法与Stream的方法一一对应
     */
    private static final Map<Method, Method> METHOD_MAP =
            Stream.of(EnhancedStream.class.getMethods())
                    .filter(m -> !m.equals(ENHANCED_DISTINCT))
                    .filter(m -> !Modifier.isStatic(m.getModifiers()))
                    .collect(Collectors.toUnmodifiableMap(
                            Function.identity(),
                            m -> {
                                try {
                                    return Stream.class.getMethod(
                                            m.getName(), m.getParameterTypes());
                                } catch (NoSuchMethodException e) {
                                    throw new Error(e);
                                }
                            }));


    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.equals(ENHANCED_DISTINCT)) {
            //调用方法为扩展方法distinct
            return distinct(
                    (EnhancedStream<T>) proxy,
                    (ToIntFunction<T>) args[0],
                    (BiPredicate<T, T>) args[1],
                    (BinaryOperator<T>) args[2]);
        } else if (method.getReturnType() == EnhancedStream.class) {
            //对于返回类型为EnhancedStream的，证明是代理的方法调用，走代理
            Method match = METHOD_MAP.get(method);
            //更相信代理对象为新的Stream
            this.delegate = (Stream) match.invoke(this.delegate, args);
            return proxy;
        } else {
            //否则，直接用代理类调用
            return method.invoke(this.delegate, args);
        }
    }

    private static final class Key<E> {
        private final E e;
        private final ToIntFunction<E> hashCode;
        private final BiPredicate<E, E> equals;

        public Key(E e, ToIntFunction<E> hashCode,
                   BiPredicate<E, E> equals) {
            this.e = e;
            this.hashCode = hashCode;
            this.equals = equals;
        }

        @Override
        public int hashCode() {
            return hashCode.applyAsInt(e);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            @SuppressWarnings("unchecked")
            Key<E> that = (Key<E>) obj;
            return equals.test(this.e, that.e);
        }
    }

    private EnhancedStream<T> distinct(EnhancedStream<T> proxy,
                                       ToIntFunction<T> hashCode,
                                       BiPredicate<T, T> equals,
                                       BinaryOperator<T> merger) {
        delegate = delegate.collect(Collectors.toMap(
                t -> new Key<>(t, hashCode, equals),
                Function.identity(),
                merger,
                //使用LinkedHashMap，保持入参原始顺序
                LinkedHashMap::new))
                .values()
                .stream();
        return proxy;
    }
}
