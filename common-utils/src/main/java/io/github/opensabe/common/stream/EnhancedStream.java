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

//import java.lang.reflect.Proxy;

import java.util.Comparator;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

public interface EnhancedStream<T> extends Stream<T> {
    EnhancedStream<T> distinct(ToIntFunction<T> hashCode,
                               BiPredicate<T, T> equals,
                               BinaryOperator<T> merger);

    @Override
    EnhancedStream<T> filter(Predicate<? super T> predicate);

    @Override
    <R> EnhancedStream<R> map(
            Function<? super T, ? extends R> mapper);

    @Override
    <R> EnhancedStream<R> flatMap(
            Function<? super T, ? extends Stream<? extends R>> mapper);

    @Override
    EnhancedStream<T> distinct();

    @Override
    EnhancedStream<T> sorted();

    @Override
    EnhancedStream<T> sorted(Comparator<? super T> comparator);

    @Override
    EnhancedStream<T> peek(Consumer<? super T> action);

    @Override
    EnhancedStream<T> limit(long maxSize);

    @Override
    EnhancedStream<T> skip(long n);

    @Override
    EnhancedStream<T> takeWhile(Predicate<? super T> predicate);

    @Override
    EnhancedStream<T> dropWhile(Predicate<? super T> predicate);

    @Override
    EnhancedStream<T> sequential();

    @Override
    EnhancedStream<T> parallel();

    @Override
    EnhancedStream<T> unordered();

    @Override
    EnhancedStream<T> onClose(Runnable closeHandler);
}
