package io.github.opensabe.common.buffer;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 内存批次队列，需要同步等待完成，并且将结果填充
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
