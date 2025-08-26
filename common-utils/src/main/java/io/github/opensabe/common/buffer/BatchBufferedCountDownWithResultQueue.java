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
package io.github.opensabe.common.buffer;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/**
 * 内存批次队列，需要同步等待完成，并且将结果填充
 *
 * @param <T>
 * @param <E>
 */
public abstract class BatchBufferedCountDownWithResultQueue<T, E extends BufferedCountDownLatchWithResultElement<T>> extends BatchBufferedCountDownQueue<E> {
    private ThreadLocal<List<T>> result = new ThreadLocal<>();

    @Override
    protected void batchManipulate(List<E> batch) {
        result.set(batchManipulateWithResult(batch));
    }

    @Override
    protected void afterBatchFinish(List<E> batch) {
        List<T> list = result.get();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            batch.get(i).finish(list.get(i));
        }
    }

    /**
     * @param batch
     * @return 与 batch 中的元素一一对应的结果
     */
    @NotNull
    protected abstract List<T> batchManipulateWithResult(List<E> batch);
}
