package io.github.opensabe.common.redisson.observation.rlock;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class RLockAcquiredContext extends Observation.Context {

    private final String lockName;
    /**
     * 是否是 try 获取
     */
    private final boolean tryAcquire;
    private final long waitTime;
    private final long leaseTime;
    private final TimeUnit timeUnit;
    private final Class lockClass;
    private final String threadName;

    /**
     * 是否成功获取到了锁
     */
    private boolean lockAcquiredSuccessfully = false;

    public RLockAcquiredContext(String lockName, boolean tryAcquire, long waitTime, long leaseTime, TimeUnit timeUnit, Class lockClass) {
        this.lockName = lockName;
        this.tryAcquire = tryAcquire;
        this.waitTime = waitTime;
        this.leaseTime = leaseTime;
        this.timeUnit = timeUnit;
        this.lockClass = lockClass;
        this.threadName = Thread.currentThread().getName();
    }
}
