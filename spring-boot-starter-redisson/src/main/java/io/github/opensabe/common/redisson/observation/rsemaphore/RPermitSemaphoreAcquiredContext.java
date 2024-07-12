package io.github.opensabe.common.redisson.observation.rsemaphore;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class RPermitSemaphoreAcquiredContext extends Observation.Context {
    private final String semaphoreName;
    private final String threadName;
    private final boolean tryAcquire;
    private final long waitTime;
    private final long leaseTime;
    private final TimeUnit unit;
    private String permitId;

    public RPermitSemaphoreAcquiredContext(String semaphoreName, boolean tryAcquire, long waitTime, long leaseTime, TimeUnit unit) {
        this.semaphoreName = semaphoreName;
        this.threadName = Thread.currentThread().getName();
        this.tryAcquire = tryAcquire;
        this.waitTime = waitTime;
        this.leaseTime = leaseTime;
        this.unit = unit;
    }
}
