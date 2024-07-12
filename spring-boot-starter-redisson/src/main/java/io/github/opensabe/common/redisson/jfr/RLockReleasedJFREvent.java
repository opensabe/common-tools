package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.rlock.RLockReleaseContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Category({"observation", "redisson", "rlock"})
@Label("release lock")
@StackTrace(false)
public class RLockReleasedJFREvent extends Event {
    @Label("lock name")
    private final String lockName;
    @Label("lock class")
    private final String lockClass;
    @Label("is lock released successfully")
    private boolean lockReleasedSuccessfully;
    private String traceId;
    private String spanId;

    public RLockReleasedJFREvent(RLockReleaseContext rLockReleaseContext) {
        this.lockName = rLockReleaseContext.getLockName();
        this.lockClass = rLockReleaseContext.getLockClass().getSimpleName();
    }
}
