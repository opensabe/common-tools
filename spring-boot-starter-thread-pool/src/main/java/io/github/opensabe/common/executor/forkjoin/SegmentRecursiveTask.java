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


import io.micrometer.observation.Observation;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public abstract class SegmentRecursiveTask<T,R> extends ListableRecursiveTask<T,R> {

    protected abstract SegmentRecursiveTask<T,R> clone(List<T> current) ;

    protected SegmentRecursiveTask(int capacity, List<T> list, Function<T, R> transformer, Function<List<R>, R> combiner, BinaryOperator<R> reducer, Observation observation) {
        super(capacity, list, transformer, combiner, reducer,observation);
    }

    public SegmentRecursiveTask(int capacity, List<T> list, Function<T, R> transformer, Function<List<R>, R> combiner, Observation observation) {
        super(capacity, list, transformer, combiner, observation);
    }

    public SegmentRecursiveTask(int capacity, List<T> list, Function<T, R> transformer, BinaryOperator<R> reducer, Observation observation) {
        super(capacity, list, transformer, reducer, observation);
    }

    @Override
    protected List<ListableRecursiveTask<T, R>> segmentation() {
        if (list.size() > capacity) {
            var left = clone(list.subList(0,capacity));
            var right = clone(list.subList(capacity,list.size()));
            return List.of(left,right);
        }
        return null;
    }

}
