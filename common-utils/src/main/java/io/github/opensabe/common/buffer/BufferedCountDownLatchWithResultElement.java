package io.github.opensabe.common.buffer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class BufferedCountDownLatchWithResultElement<RESULT> extends BufferedCountDownLatchElement {
    private volatile RESULT result;

    RESULT waitResultUntilFinish() {
        super.waitUntilFinish();
        return result;
    }

    void finish(RESULT result) {
        log.info("BufferedCountDownLatchWithResultElement-finish: origin traceId: {} spanId: {}, result {}", traceId(), spanId(), result);
        this.result = result;
        super.finish();
    }

    public RESULT getResult() {
        return result;
    }
}
