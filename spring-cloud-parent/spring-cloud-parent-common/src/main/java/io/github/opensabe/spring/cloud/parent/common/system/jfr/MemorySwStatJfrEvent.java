package io.github.opensabe.spring.cloud.parent.common.system.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;

@Category({"Native Memory Tracking"})
@Label("Memory Sw Stat")
public class MemorySwStatJfrEvent extends Event {

    private long usageInBytes;
    private long maxUsageInBytes;
    private long limitInBytes;

    public MemorySwStatJfrEvent(long usageInBytes, long maxUsageInBytes, long limitInBytes) {
        this.usageInBytes = usageInBytes;
        this.maxUsageInBytes = maxUsageInBytes;
        this.limitInBytes = limitInBytes;
    }
}
