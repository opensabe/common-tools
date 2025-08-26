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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;

import io.micrometer.observation.Observation;

/**
 * 简单的把原集合里的元素转换成新的元素，并返回新的列表
 *
 * @param <T> 原集合的元素
 * @param <R> 新集合的元素
 * @author heng.ma
 */
public class MapListableRecursiveTask<T, R> extends SegmentRecursiveTask<T, List<R>> {

    private final Function<T, R> transformer;

    public MapListableRecursiveTask(int capacity, List<T> list, Function<T, R> transformer, Observation observation) {
        super(capacity, list, null, null, null, observation);
        this.transformer = transformer;
    }

    @Override
    protected List<R> compute0() {
        var task = segmentation();
        if (CollectionUtils.isEmpty(task)) {
            return list.stream().map(transformer).collect(Collectors.toList());
        }
        return aggregate(invokeAll(task).stream().map(ListableRecursiveTask::join));
    }

    @Override
    protected List<R> aggregate(Stream<List<R>> result) {
        return result.flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    protected SegmentRecursiveTask<T, List<R>> clone(List<T> current) {
        return new MapListableRecursiveTask<>(capacity, current, transformer, observation);
    }
}
