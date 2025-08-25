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
import java.util.concurrent.ForkJoinTask;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;

import io.micrometer.observation.Observation;

/**
 * 处理集合数据
 *
 * @param <T>
 * @param <R>
 */
public class BatchRecursiveTask<T, R> extends SegmentRecursiveTask<T, R> {
    private final Function<List<T>, R> transformer;

    protected BatchRecursiveTask(int capacity, List<T> list, Function<List<T>, R> transformer, Function<List<R>, R> combiner, Observation observation) {
        this(capacity, list, transformer, null, combiner, observation);
    }

    protected BatchRecursiveTask(int capacity, List<T> list, Function<List<T>, R> transformer, BinaryOperator<R> reducer, Observation observation) {
        this(capacity, list, transformer, reducer, null, observation);
    }

    private BatchRecursiveTask(int capacity, List<T> list, Function<List<T>, R> transformer, BinaryOperator<R> reducer, Function<List<R>, R> combiner, Observation observation) {
        super(capacity, list, null, combiner, reducer, observation);
        this.transformer = transformer;
    }

    @Override
    protected R compute0() {
        var tasks = segmentation();
        if (CollectionUtils.isEmpty(tasks)) {
            return transformer.apply(list);
        }
        return aggregate(invokeAll(tasks).stream().map(ForkJoinTask::join));
    }

    @Override
    protected R aggregate(Stream<R> result) {
        return reducer == null ?
                combiner.apply(result.collect(Collectors.toList())) :
                result.reduce(reducer).orElse(null);
    }

    @Override
    protected SegmentRecursiveTask<T, R> clone(List<T> current) {
        return new BatchRecursiveTask<>(capacity, current, this.transformer, reducer, combiner, observation);
    }
}
