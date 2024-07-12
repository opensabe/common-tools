package io.github.opensabe.common.buffer;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import jdk.jfr.Timespan;
import jdk.jfr.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * 记录每个提交到队列的 JFR 事件
 */
@Category({"Executor Service"})
@Label("Buffered Element JFR")
@StackTrace(false)
@Getter
@Setter
public class BufferedElementJFREvent extends Event {
    @Label("traceId when submit")
    private String submitTraceId;
    @Label("spanId when submit")
    private String submitSpanId;
    @Label("spanId when batch manipulate")
    private String batchSpanId;
    @Label("submitTime")
    @Timestamp(value = Timestamp.MILLISECONDS_SINCE_EPOCH)
    @Description("the time when submit to queue")
    private final long submitTime;
    @Timespan(value = Timespan.MILLISECONDS)
    @Description("the time in queue")
    private long queueTime;
    @Label("error")
    private String error;

    public BufferedElementJFREvent() {
        //nanoseconds 的速度在一些系统比 currentTimeMillis 快
        submitTime = System.currentTimeMillis();
    }
}
