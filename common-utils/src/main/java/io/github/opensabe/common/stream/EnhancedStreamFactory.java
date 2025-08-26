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

import java.lang.reflect.Proxy;
import java.util.stream.Stream;

public class EnhancedStreamFactory {
    @SuppressWarnings("unchecked")
    public static <E> EnhancedStream<E> newEnhancedStream(Stream<E> stream) {
        return (EnhancedStream<E>) Proxy.newProxyInstance(
                //必须用EnhancedStream的classLoader，不能用Stream的，因为Stream是jdk的类，ClassLoader是rootClassLoader
                EnhancedStream.class.getClassLoader(),
                //代理接口
                new Class<?>[]{EnhancedStream.class},
                //代理类
                new EnhancedStreamHandler<>(stream)
        );
    }
}
