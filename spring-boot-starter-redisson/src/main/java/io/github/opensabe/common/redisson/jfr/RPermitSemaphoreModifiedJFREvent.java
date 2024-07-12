package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.rsemaphore.RPermitSemaphoreModifiedContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Category({"observation", "redisson", "rPermitSemaphore"})
@Label("modified")
@StackTrace(false)
public class RPermitSemaphoreModifiedJFREvent extends Event {
    @Label("permit semaphore name")
    private final String permitSemaphoreName;
    @Label("modified")
    private final String modified;
    @Label("is modified successfully")
    private boolean modifiedSuccessfully;
    private String traceId;
    private String spanId;

    public RPermitSemaphoreModifiedJFREvent(RPermitSemaphoreModifiedContext rPermitSemaphoreModifiedContext) {
        this.permitSemaphoreName = rPermitSemaphoreModifiedContext.getSemaphoreName();
        this.modified = rPermitSemaphoreModifiedContext.getModified();
    }
}
