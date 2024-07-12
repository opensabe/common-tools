package io.github.opensabe.common.executor.jfr;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jdk.jfr.*;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("unused")
@Category({"Executor Service"})
@Label("Thread Task JFR (default Threshold 100ms)")
@Description("it manages to record the traceid and the start time of the task and the time-consuming of the end")
@StackTrace(false)
@Threshold(value = "100ms")
@SuppressFBWarnings("URF_UNREAD_FIELD")
public class ThreadTaskJFREvent extends Event {

    @Getter
    private final String traceId;

    @Getter
    private final String spanId;

    @Getter
    @Label("taskSubmitStartTime")
//    @Timestamp(value = Timestamp.MILLISECONDS_SINCE_EPOCH)
    @Description("the time when the task starts to be put in the thread pool")
    private final long submitTaskStartTime;

    @Setter
    @Getter
    @Label("taskRunStartTime")
//    @Timestamp(value = Timestamp.MILLISECONDS_SINCE_EPOCH)
    @Description("the time when the task starts to run")
    private long taskRunStartTime;

    @Setter
    @Getter
    @Label("taskRunEndTime")
//    @Timestamp(value = Timestamp.MILLISECONDS_SINCE_EPOCH)
    @Description("the time when the task has finished")
    private long taskRunEndTime;

    @Setter
    @Timespan(value = Timespan.MILLISECONDS)
    @Description("the time-consuming of the span of the task with the lifecycle of the running state without queuing")
    private long taskQueueTimeDuration;

    @Setter
    @Timespan(value = Timespan.MILLISECONDS)
    @Description("the time-consuming of the span of the task with the lifecycle of queuing")
    private long taskRunTimeDuration;

    public ThreadTaskJFREvent(long submitStartTime, String traceId, String spanId) {
        this.submitTaskStartTime = submitStartTime;
        this.traceId = traceId;
        this.spanId = spanId;
    }

}