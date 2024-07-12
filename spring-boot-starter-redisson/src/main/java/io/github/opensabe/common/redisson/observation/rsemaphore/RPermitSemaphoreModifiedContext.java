package io.github.opensabe.common.redisson.observation.rsemaphore;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RPermitSemaphoreModifiedContext extends Observation.Context {
    private final String semaphoreName;
    private final String threadName;
    private final String modified;

    private boolean modifiedSuccessfully;

    public RPermitSemaphoreModifiedContext(String semaphoreName, String modified) {
        this.semaphoreName = semaphoreName;
        this.threadName = Thread.currentThread().getName();
        this.modified = modified;
    }
}
