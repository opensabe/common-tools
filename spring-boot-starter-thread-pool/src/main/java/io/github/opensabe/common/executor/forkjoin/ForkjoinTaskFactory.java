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
package io.github.opensabe.common.executor.forkjoin;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;

public class ForkjoinTaskFactory {

    private final UnifiedObservationFactory unifiedObservationFactory;

    public ForkjoinTaskFactory(UnifiedObservationFactory unifiedObservationFactory) {
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    /**
     * 单个任务也是处理列表，action没有返回值，只是异步提交任务
     *
     * @param capacity 每个任务分配的数量
     * @param list     要处理的列表
     * @param consumer 列表的处理方法
     * @param <T>      列表保存的数据类型
     * @return
     */
    public <T> BatchRecursiveAction<T> recursiveBatchAction(int capacity, List<T> list, Consumer<List<T>> consumer) {
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        return new BatchRecursiveAction<>(capacity, list, consumer, observation);
    }

    /**
     * 将列表转化为一个对象，并将各个子任务生成的结果合并
     *
     * @param capacity 每个任务分配的数量
     * @param list     要处理的列表
     * @param function 将多条数据转换为一个结果
     * @param combiner 合并多个结果
     * @param <T>
     * @param <R>
     * @return
     */
    public <T, R> BatchRecursiveTask<T, R> recursiveBatchTask(int capacity, List<T> list, Function<List<T>, R> function, Function<List<R>, R> combiner) {
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        return new BatchRecursiveTask<>(capacity, list, function, combiner, observation);
    }

    public <T, R> BatchRecursiveTask<T, R> recursiveBatchTask(int capacity, List<T> list, Function<List<T>, R> function, BinaryOperator<R> reducer) {
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        return new BatchRecursiveTask<>(capacity, list, function, reducer, observation);
    }

    /**
     * 单独处理一个数据的action,与{@link ForkjoinTaskFactory#recursiveBatchTask(int, List, Function, Function)}不同的是，
     * 该对象的业务处理（consumer）是一条一条处理
     *
     * @param capacity 每个任务分配的数量
     * @param list     要处理的列表
     * @param consumer 列表的处理方法
     * @param <T>      列表保存的数据类型
     * @return
     */
    public <T> ListableRecursiveAction<T> recursiveAction(int capacity, List<T> list, Consumer<T> consumer) {
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        return new ListableRecursiveAction<>(capacity, list, consumer, observation);
    }

    /**
     * 将list里保存的对象T转换为V，并将新的列表返回
     *
     * @param capacity    每个任务分配的数量
     * @param list        要处理的列表
     * @param transformer 转换方法
     * @param <T>         list原始数据类型
     * @param <V>         返回的集合数据类型
     * @return
     */
    public <T, V> TraceableRecursiveTask<List<V>> mapListableTask(int capacity, List<T> list, Function<T, V> transformer) {
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        return new MapListableRecursiveTask<>(capacity, list, transformer, observation);
    }

    /**
     * 将集合聚合成新的对象返回
     *
     * @param capacity    每个任务分配的数量
     * @param list        要处理的列表
     * @param transformer 集合中每个元素的处理方法
     * @param combiner    将新生成的集合结果合并成最终结果
     * @param <T>         list原始数据类型
     * @param <V>         最终合并生成的结果
     * @return
     */
    public <T, V> TraceableRecursiveTask<V> combinerListableTask(int capacity, List<T> list, Function<T, V> transformer, Function<List<V>, V> combiner) {
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        return new AggregateRecursiveTask<>(capacity, list, transformer, combiner, observation);
    }

    public <T, V> TraceableRecursiveTask<V> reducerListableTask(int capacity, List<T> list, Function<T, V> transformer, BinaryOperator<V> reducer) {
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        return new AggregateRecursiveTask<>(capacity, list, transformer, reducer, observation);
    }

}
