package io.github.opensabe.common.buffer;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public abstract class BufferedCountDownLatchElement extends BufferedElement {
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final AtomicBoolean finished = new AtomicBoolean(false);

    private volatile Throwable throwable;

    void waitUntilFinish() {
        boolean completed = false;
        long startTime = System.currentTimeMillis();
        while (!completed) {
            try {
                countDownLatch.await();
                completed = true;
                log.info("BufferedCountDownLatchElement-waitUntilFinish origin traceId: {} spanId: {} elapsed {}ms", traceId(), spanId(), (System.currentTimeMillis() - startTime));
            } catch (Throwable e) {
                log.info("BufferedCountDownLatchElement-waitUntilFinish await error: {}", e.getMessage(), e);
                //线程所在的线程池被关闭的时候，可能会调用 interrupt，导致从 await() 返回
                //对于这些异常，我们忽略
            }
        }
        if (throwable != null) {
            throw new RuntimeException(throwable);
        }
    }

    void finish() {
        if (finished.compareAndSet(false, true)) {
            countDownLatch.countDown();
        }
    }

    /**
     * 可以由外部调用
     * @param throwable
     */
    public void error(Throwable throwable) {
        this.throwable = throwable;
        finish();
    }
}
