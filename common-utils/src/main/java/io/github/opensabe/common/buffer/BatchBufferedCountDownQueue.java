package io.github.opensabe.common.buffer;

import java.util.List;

/**
 * 内存批次队列，需要同步等待完成
 * 用 countDownLatch 的原因是可能会有很多
 */
//@Log4j2
public abstract class BatchBufferedCountDownQueue<E extends BufferedCountDownLatchElement> extends BatchBufferedQueue<E> {
    @Override
    protected void afterBatchFinish(List<E> batch) {
        batch.forEach(BufferedCountDownLatchElement::finish);
    }

    @Override
    protected void afterBatchError(List<E> batch, Throwable throwable) {
        batch.forEach(e -> e.error(throwable));
    }

    /**
     * 目前不能在外层有事务，因为这里面的东西不会因为外层的事务回滚而回滚
     * @param e
     */
    @Override
    public void submit(E e) {
        super.submit(e);
        e.waitUntilFinish();
    }
}
