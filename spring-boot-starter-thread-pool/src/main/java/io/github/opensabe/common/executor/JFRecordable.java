package io.github.opensabe.common.executor;

import io.github.opensabe.common.executor.jfr.ThreadTaskJFREvent;

public interface JFRecordable<V> {

    ThreadTaskJFREvent getEvent ();

    default V record () {
        var threadTaskJFREvent = getEvent();
        threadTaskJFREvent.setTaskRunStartTime(System.currentTimeMillis());
        threadTaskJFREvent.setTaskQueueTimeDuration(threadTaskJFREvent.getTaskRunStartTime() - threadTaskJFREvent.getSubmitTaskStartTime());
        try {
            return inRecord();
        }finally {
            threadTaskJFREvent.setTaskRunEndTime(System.currentTimeMillis());
            threadTaskJFREvent.setTaskRunTimeDuration(threadTaskJFREvent.getTaskRunEndTime() - threadTaskJFREvent.getTaskRunStartTime());
            threadTaskJFREvent.commit();
        }
    }

    V inRecord () ;
}
