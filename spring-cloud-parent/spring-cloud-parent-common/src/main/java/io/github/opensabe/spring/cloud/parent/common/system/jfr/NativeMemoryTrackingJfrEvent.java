package io.github.opensabe.spring.cloud.parent.common.system.jfr;

import jdk.jfr.*;
import lombok.Getter;

@Category({"Native Memory Tracking"})
@Label("Native Memory Tracking JFR")
@Description("it manages to record the metrics in Native memory Tracking such as reserved and committed")
@StackTrace(false)
public class NativeMemoryTrackingJfrEvent extends Event {

    @Getter
    @Label("metric name")
    private final String name;
    @Getter
    @Label("reserved value")
    private final long reserved;
    @Getter
    @Label("committed value")
    private final long committed;

    public NativeMemoryTrackingJfrEvent(String name, long reserved, long committed) {
        this.name = name;
        this.reserved = reserved;
        this.committed = committed;
    }
}
