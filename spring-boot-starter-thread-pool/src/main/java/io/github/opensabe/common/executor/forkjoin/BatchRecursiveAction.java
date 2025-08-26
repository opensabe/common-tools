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
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;

import io.micrometer.observation.Observation;

/**
 * 单个任务也是批量处理，仅仅是将大的列表切成小列表处理
 *
 * @param <T> 要处理的集合里的数据类型
 * @author heng.ma
 */
public class BatchRecursiveAction<T> extends SegmentRecursiveTask<T, Void> {
    protected Consumer<List<T>> batchConsumer;

    protected BatchRecursiveAction(int capacity, List<T> list, Consumer<List<T>> batchConsumer, Observation observation) {
        super(capacity, list, null, null, null, observation);
        this.batchConsumer = batchConsumer;
    }

    @Override
    protected Void compute0() {
        var tasks = segmentation();
        if (CollectionUtils.isEmpty(tasks)) {
            batchConsumer.accept(list);
        } else {
            invokeAll(tasks).forEach(ListableRecursiveTask::join);
        }
        return null;
    }

    @Override
    protected Void aggregate(Stream<Void> result) {
        return null;
    }

    @Override
    protected SegmentRecursiveTask<T, Void> clone(List<T> current) {
        return new BatchRecursiveAction<>(capacity, current, batchConsumer, observation);
    }
}
