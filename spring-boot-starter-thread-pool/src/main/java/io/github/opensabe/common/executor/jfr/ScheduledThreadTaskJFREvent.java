package io.github.opensabe.common.executor.jfr;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jdk.jfr.*;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Category({"Executor Service"})
@Label("Scheduled Thread Task JFR")
@Description("it manages to record the traceid and the start time of the task and the time-consuming of the end")
@StackTrace(false)
@SuppressFBWarnings("URF_UNREAD_FIELD")
public class ScheduledThreadTaskJFREvent extends Event {

    @Getter
    private final String traceId;

    @Getter
    private final String spanId;
    private final long initialDelay;
    private final long period;
    private final long delay;
    private final TimeUnit unit;

    @Setter
    @Getter
    @Label("taskRunStartTime")
    @Timestamp(value = Timestamp.MILLISECONDS_SINCE_EPOCH)
    @Description("the time when the task starts to run")
    private long taskRunStartTime;

    @Setter
    @Getter
    @Label("taskRunEndTime")
    @Timestamp(value = Timestamp.MILLISECONDS_SINCE_EPOCH)
    @Description("the time when the task has finished")
    private long taskRunEndTime;

    @Setter
    @Timespan(value = Timespan.MILLISECONDS)
    @Description("the time-consuming of the span of the task with the lifecycle of queuing")
    private long taskRunTimeDuration;

    public ScheduledThreadTaskJFREvent(String traceId, String spanId, long initialDelay, long period, long delay, TimeUnit unit) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.initialDelay = initialDelay;
        this.period = period;
        this.delay = delay;
        this.unit = unit;
    }
}
