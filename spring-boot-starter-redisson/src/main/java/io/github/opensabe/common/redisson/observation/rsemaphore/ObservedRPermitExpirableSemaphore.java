package io.github.opensabe.common.redisson.observation.rsemaphore;

import io.github.opensabe.common.redisson.observation.rexpirable.ObservedRExpirable;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import org.redisson.api.RFuture;
import org.redisson.api.RPermitExpirableSemaphore;

import java.util.concurrent.TimeUnit;

public class ObservedRPermitExpirableSemaphore extends ObservedRExpirable implements RPermitExpirableSemaphore {
    private final RPermitExpirableSemaphore delegate;

    public ObservedRPermitExpirableSemaphore(RPermitExpirableSemaphore delegate, UnifiedObservationFactory unifiedObservationFactory) {
        super(delegate, unifiedObservationFactory);
        this.delegate = delegate;
    }

    interface PermitSemaphoreAcquire {
        String run();
    }

    private String observeAcquiringPermitSemaphore(boolean tryAcquire, long waitTime, long leaseTime, TimeUnit unit, PermitSemaphoreAcquire permitSemaphoreAcquire) {
        RPermitSemaphoreAcquiredContext rPermitSemaphoreAcquiredContext = new RPermitSemaphoreAcquiredContext(
                getName(), tryAcquire, waitTime, leaseTime, unit
        );
        Observation observation = RPermitSemaphoreObservationDocumentation.SEMAPHORE_ACQUIRE.observation(
                null, RPermitSemaphoreAcquiredObservationConvention.DEFAULT,
                () -> rPermitSemaphoreAcquiredContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            String result = permitSemaphoreAcquire.run();
            rPermitSemaphoreAcquiredContext.setPermitId(result);
            return result;
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }


    @Override
    public String acquire() throws InterruptedException {
        try {
            return observeAcquiringPermitSemaphore(false, -1L, -1L, TimeUnit.SECONDS, () -> {
                try {
                    return delegate.acquire();
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
    public String acquire(long leaseTime, TimeUnit unit) throws InterruptedException {
        return observeAcquiringPermitSemaphore(false, -1L, leaseTime, unit, () -> {
            try {
                return delegate.acquire(leaseTime, unit);
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    @Override
    public String tryAcquire() {
        return observeAcquiringPermitSemaphore(
                true, -1L, -1L, TimeUnit.SECONDS, delegate::tryAcquire
        );
    }

    @Override
    public String tryAcquire(long waitTime, TimeUnit unit) throws InterruptedException {
        try {
            return observeAcquiringPermitSemaphore(
                    true, waitTime, -1L, unit, () -> {
                        try {
                            return delegate.tryAcquire(waitTime, unit);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    }
            );
        } catch (RuntimeException e) {
            //为了保持原来的表现一致，需要解封出原始的异常
            if (e.getCause() instanceof InterruptedException interruptedException) {
                throw interruptedException;
            }
            throw e;
        }
    }

    @Override
    public String tryAcquire(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        try {
            return observeAcquiringPermitSemaphore(
                    true, waitTime, leaseTime, unit, () -> {
                        try {
                            return delegate.tryAcquire(waitTime, leaseTime, unit);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    }
            );
        } catch (RuntimeException e) {
            //为了保持原来的表现一致，需要解封出原始的异常
            if (e.getCause() instanceof InterruptedException interruptedException) {
                throw interruptedException;
            }
            throw e;
        }
    }

    @Override
    public boolean tryRelease(String permitId) {
        RPermitSemaphoreReleasedContext rPermitSemaphoreReleasedContext = new RPermitSemaphoreReleasedContext(
                getName(), permitId
        );
        Observation observation = RPermitSemaphoreObservationDocumentation.SEMAPHORE_RELEASE.observation(
                null, RPermitSemaphoreReleasedObservationConvention.DEFAULT,
                () -> rPermitSemaphoreReleasedContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            boolean result = delegate.tryRelease(permitId);
            rPermitSemaphoreReleasedContext.setPermitReleasedSuccessfully(result);
            return result;
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public void release(String permitId) {
        RPermitSemaphoreReleasedContext rPermitSemaphoreReleasedContext = new RPermitSemaphoreReleasedContext(
                getName(), permitId
        );
        Observation observation = RPermitSemaphoreObservationDocumentation.SEMAPHORE_RELEASE.observation(
                null, RPermitSemaphoreReleasedObservationConvention.DEFAULT,
                () -> rPermitSemaphoreReleasedContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            delegate.release(permitId);
            rPermitSemaphoreReleasedContext.setPermitReleasedSuccessfully(true);
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public int availablePermits() {
        return delegate.availablePermits();
    }

    @Override
    public boolean trySetPermits(int permits) {
        RPermitSemaphoreModifiedContext rPermitSemaphoreModifiedContext = new RPermitSemaphoreModifiedContext(
                getName(), "trySetPermits: " + permits
        );
        Observation observation = RPermitSemaphoreObservationDocumentation.PERMIT_MODIFIED.observation(
                null, RPermitSemaphoreModifiedObservationConvention.DEFAULT,
                () -> rPermitSemaphoreModifiedContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            boolean result = delegate.trySetPermits(permits);
            rPermitSemaphoreModifiedContext.setModifiedSuccessfully(result);
            return result;
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public void addPermits(int permits) {
        RPermitSemaphoreModifiedContext rPermitSemaphoreModifiedContext = new RPermitSemaphoreModifiedContext(
                getName(), "addPermits: " + permits
        );
        Observation observation = RPermitSemaphoreObservationDocumentation.PERMIT_MODIFIED.observation(
                null, RPermitSemaphoreModifiedObservationConvention.DEFAULT,
                () -> rPermitSemaphoreModifiedContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            delegate.addPermits(permits);
            rPermitSemaphoreModifiedContext.setModifiedSuccessfully(true);
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public boolean updateLeaseTime(String permitId, long leaseTime, TimeUnit unit) {
        RPermitSemaphoreModifiedContext rPermitSemaphoreModifiedContext = new RPermitSemaphoreModifiedContext(
                getName(), "updateLeaseTime: " + permitId + ", " + leaseTime + ", " + unit
        );
        Observation observation = RPermitSemaphoreObservationDocumentation.PERMIT_MODIFIED.observation(
                null, RPermitSemaphoreModifiedObservationConvention.DEFAULT,
                () -> rPermitSemaphoreModifiedContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            boolean result = delegate.updateLeaseTime(permitId, leaseTime, unit);
            rPermitSemaphoreModifiedContext.setModifiedSuccessfully(result);
            return result;
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public RFuture<String> acquireAsync() {
        return delegate.acquireAsync();
    }

    @Override
    public RFuture<String> acquireAsync(long leaseTime, TimeUnit unit) {
        return delegate.acquireAsync(leaseTime, unit);
    }

    @Override
    public RFuture<String> tryAcquireAsync() {
        return delegate.tryAcquireAsync();
    }

    @Override
    public RFuture<String> tryAcquireAsync(long waitTime, TimeUnit unit) {
        return delegate.tryAcquireAsync(waitTime, unit);
    }

    @Override
    public RFuture<String> tryAcquireAsync(long waitTime, long leaseTime, TimeUnit unit) {
        return delegate.tryAcquireAsync(waitTime, leaseTime, unit);
    }

    @Override
    public RFuture<Boolean> tryReleaseAsync(String permitId) {
        return delegate.tryReleaseAsync(permitId);
    }

    @Override
    public RFuture<Void> releaseAsync(String permitId) {
        return delegate.releaseAsync(permitId);
    }

    @Override
    public RFuture<Integer> availablePermitsAsync() {
        return delegate.availablePermitsAsync();
    }

    @Override
    public RFuture<Boolean> trySetPermitsAsync(int permits) {
        return delegate.trySetPermitsAsync(permits);
    }

    @Override
    public RFuture<Void> addPermitsAsync(int permits) {
        return delegate.addPermitsAsync(permits);
    }

    @Override
    public RFuture<Boolean> updateLeaseTimeAsync(String permitId, long leaseTime, TimeUnit unit) {
        return delegate.updateLeaseTimeAsync(permitId, leaseTime, unit);
    }
}
