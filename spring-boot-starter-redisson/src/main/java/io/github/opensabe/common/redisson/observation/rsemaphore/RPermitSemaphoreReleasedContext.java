package io.github.opensabe.common.redisson.observation.rsemaphore;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RPermitSemaphoreReleasedContext extends Observation.Context {
    private final String semaphoreName;
    private final String threadName;
    private final String permitId;
    private boolean permitReleasedSuccessfully;

    public RPermitSemaphoreReleasedContext(String semaphoreName, String permitId) {
        this.semaphoreName = semaphoreName;
        this.threadName = Thread.currentThread().getName();
        this.permitId = permitId;
    }
}
