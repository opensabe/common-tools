package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.rsemaphore.RPermitSemaphoreReleasedContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Category({"observation", "redisson", "rPermitSemaphore"})
@Label("released")
@StackTrace(false)
public class RPermitSemaphoreReleasedJFREvent extends Event {
    @Label("permit semaphore name")
    private final String permitSemaphoreName;
    @Label("permit id")
    private final String permitId;
    @Label("is permit released successfully")
    private boolean permitReleasedSuccessfully;
    private String traceId;
    private String spanId;

    public RPermitSemaphoreReleasedJFREvent(RPermitSemaphoreReleasedContext rPermitSemaphoreReleasedContext) {
        this.permitSemaphoreName = rPermitSemaphoreReleasedContext.getSemaphoreName();
        this.permitId = rPermitSemaphoreReleasedContext.getPermitId();
    }
}
