package io.github.opensabe.common.redisson.observation.rlock;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RLockReleaseContext extends Observation.Context {

    private final String lockName;
    private final Class lockClass;
    private final String threadName;

    /**
     * 是否成功释放了锁
     */
    private boolean lockReleasedSuccessfully = false;

    public RLockReleaseContext(String lockName, Class lockClass) {
        this.lockName = lockName;
        this.lockClass = lockClass;
        this.threadName = Thread.currentThread().getName();
    }
}
