package io.github.opensabe.common.redisson.observation.rlock;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * 观察者模式的分布式锁
 * 注意每次都要新建一个实例，不要重复使用
 * 通过正常的 api 就是每次新建一个，但是注意不要使用单例模式
 */
public class ObservedRLock implements RLock {
    private final RLock delegate;
    private final UnifiedObservationFactory unifiedObservationFactory;

    public ObservedRLock(RLock delegate, UnifiedObservationFactory unifiedObservationFactory) {
        this.delegate = delegate;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    interface LockAcquire {
        boolean run();
    }

    private boolean observeAcquiringLock(boolean tryAcquire, long waitTime, long leaseTime, TimeUnit unit, LockAcquire lockAcquire) {
        RLockAcquiredContext rLockAcquiredContext = new RLockAcquiredContext(
                getName(), tryAcquire, waitTime, leaseTime, unit, delegate.getClass()
        );
        Observation observation = RLockObservationDocumentation.LOCK_ACQUIRE.observation(
                null, RLockAcquiredObservationConvention.DEFAULT,
                () -> rLockAcquiredContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            boolean result = lockAcquire.run();
            rLockAcquiredContext.setLockAcquiredSuccessfully(result);
            return result;
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void lockInterruptibly(long leaseTime, TimeUnit unit) throws InterruptedException {
        try {
            observeAcquiringLock(false, -1L, leaseTime, unit, () -> {
                try {
                    delegate.lockInterruptibly(leaseTime, unit);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                return true;
            });
        } catch (RuntimeException e) {
            //为了保持原来的表现一致，需要解封出原始的异常
            if (e.getCause() instanceof InterruptedException interruptedException) {
                throw interruptedException;
            }
            throw e;
        }
    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        try {
            return observeAcquiringLock(true, waitTime, leaseTime, unit, () -> {
                try {
                    return delegate.tryLock(waitTime, leaseTime, unit);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            });
        } catch (RuntimeException e) {
            //为了保持原来的表现一致，需要解封出原始的异常
            if (e.getCause() instanceof InterruptedException interruptedException) {
                throw interruptedException;
            }
            throw e;
        }
    }

    @Override
    public void lock(long leaseTime, TimeUnit unit) {
        observeAcquiringLock(false, -1L, leaseTime, unit, () -> {
            delegate.lock(leaseTime, unit);
            return true;
        });
    }

    @Override
    public boolean forceUnlock() {
        RLockForceReleaseContext rLockForceReleaseContext = new RLockForceReleaseContext(
                getName(), delegate.getClass()
        );
        Observation observation = RLockObservationDocumentation.LOCK_FORCE_RELEASE.observation(
                null, RLockForceReleaseObservationConvention.DEFAULT,
                () -> rLockForceReleaseContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            boolean result = delegate.forceUnlock();
            rLockForceReleaseContext.setLockReleasedSuccessfully(result);
            return result;
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public boolean isLocked() {
        return delegate.isLocked();
    }

    @Override
    public boolean isHeldByThread(long threadId) {
        return delegate.isHeldByThread(threadId);
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return delegate.isHeldByCurrentThread();
    }

    @Override
    public int getHoldCount() {
        return delegate.getHoldCount();
    }

    @Override
    public long remainTimeToLive() {
        return delegate.remainTimeToLive();
    }

    @Override
    public void lock() {
        observeAcquiringLock(false, -1L, -1L, null, () -> {
            delegate.lock();
            return true;
        });
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        try {
            observeAcquiringLock(false, -1L, -1L, null, () -> {
                try {
                    delegate.lockInterruptibly();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                return true;
            });
        } catch (RuntimeException e) {
            //为了保持原来的表现一致，需要解封出原始的异常
            if (e.getCause() instanceof InterruptedException interruptedException) {
                throw interruptedException;
            }
            throw e;
        }
    }

    @Override
    public boolean tryLock() {
        return observeAcquiringLock(true, -1L, -1L, null, () -> {
            return delegate.tryLock();
        });
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        try {
            return observeAcquiringLock(true, time, -1L, unit, () -> {
                try {
                    return delegate.tryLock(time, unit);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            });
        } catch (RuntimeException e) {
            //为了保持原来的表现一致，需要解封出原始的异常
            if (e.getCause() instanceof InterruptedException interruptedException) {
                throw interruptedException;
            }
            throw e;
        }
    }

    @Override
    public void unlock() {
        RLockReleaseContext rLockReleaseContext = new RLockReleaseContext(
                getName(), delegate.getClass()
        );
        Observation observation = RLockObservationDocumentation.LOCK_RELEASE.observation(
                null, RLockReleaseObservationConvention.DEFAULT,
                () -> rLockReleaseContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            delegate.unlock();
            rLockReleaseContext.setLockReleasedSuccessfully(true);
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public Condition newCondition() {
        return delegate.newCondition();
    }


    //以下方法无法实现 Observation，需要深层次改造，但是其实我们也没用到异步的，
    //以后引入虚拟线程更用不到异步响应式编程了，所以暂时不改造了
    @Override
    public RFuture<Boolean> forceUnlockAsync() {
        return delegate.forceUnlockAsync();
    }

    @Override
    public RFuture<Void> unlockAsync() {
        return delegate.unlockAsync();
    }

    @Override
    public RFuture<Void> unlockAsync(long threadId) {
        return delegate.unlockAsync();
    }

    @Override
    public RFuture<Boolean> tryLockAsync() {
        return delegate.tryLockAsync();
    }

    @Override
    public RFuture<Void> lockAsync() {
        return delegate.lockAsync();
    }

    @Override
    public RFuture<Void> lockAsync(long threadId) {
        return delegate.lockAsync(threadId);
    }

    @Override
    public RFuture<Void> lockAsync(long leaseTime, TimeUnit unit) {
        return delegate.lockAsync(leaseTime, unit);
    }

    @Override
    public RFuture<Void> lockAsync(long leaseTime, TimeUnit unit, long threadId) {
        return delegate.lockAsync(leaseTime, unit, threadId);
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long threadId) {
        return delegate.tryLockAsync(threadId);
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long waitTime, TimeUnit unit) {
        return delegate.tryLockAsync(waitTime, unit);
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long waitTime, long leaseTime, TimeUnit unit) {
        return delegate.tryLockAsync(waitTime, leaseTime, unit);
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId) {
        return delegate.tryLockAsync(waitTime, leaseTime, unit, threadId);
    }

    @Override
    public RFuture<Integer> getHoldCountAsync() {
        return delegate.getHoldCountAsync();
    }

    @Override
    public RFuture<Boolean> isLockedAsync() {
        return delegate.isLockedAsync();
    }

    @Override
    public RFuture<Long> remainTimeToLiveAsync() {
        return delegate.remainTimeToLiveAsync();
    }
}
