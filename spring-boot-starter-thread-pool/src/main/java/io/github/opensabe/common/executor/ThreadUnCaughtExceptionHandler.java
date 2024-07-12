package io.github.opensabe.common.executor;

import lombok.extern.log4j.Log4j2;

/**
 * Author: duchaoqun
 * Date: 2021/3/2 8:00
 */
@Log4j2
public class ThreadUnCaughtExceptionHandler implements Thread.UncaughtExceptionHandler{
    public ThreadUnCaughtExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Exception in thread {},error {}"
                ,t.getName(),e.getMessage(),e);
    }
}
