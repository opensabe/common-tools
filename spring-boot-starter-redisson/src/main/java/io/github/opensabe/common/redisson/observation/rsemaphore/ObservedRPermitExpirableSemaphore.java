package io.github.opensabe.common.redisson.observation.rsemaphore;

import io.github.opensabe.common.redisson.observation.rexpirable.ObservedRExpirable;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import org.redisson.api.RFuture;
import org.redisson.api.RPermitExpirableSemaphore;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ObservedRPermitExpirableSemaphore extends ObservedRExpirable implements RPermitExpirableSemaphore {
    private final RPermitExpirableSemaphore delegate;

    public ObservedRPermitExpirableSemaphore(RPermitExpirableSemaphore delegate, UnifiedObservationFactory unifiedObservationFactory) {
        super(delegate, unifiedObservationFactory);
        this.delegate = delegate;
    }

    @Override
    public boolean copy(String destination) {
        return delegate.copy(destination);
    }

    @Override
    public boolean copy(String destination, int database) {
        return delegate.copy(destination,database);
    }

    @Override
    public boolean copyAndReplace(String destination) {
        return delegate.copyAndReplace(destination);
    }

    @Override
    public boolean copyAndReplace(String destination, int database) {
        return delegate.copyAndReplace(destination,database);
    }

    @Override
    public RFuture<Boolean> copyAsync(String destination) {
        return delegate.copyAsync(destination);
    }

    @Override
    public RFuture<Boolean> copyAsync(String destination, int database) {
        return delegate.copyAsync(destination, database);
    }

    @Override
    public RFuture<Boolean> copyAndReplaceAsync(String destination) {
        return delegate.copyAndReplaceAsync(destination);
    }

    @Override
    public RFuture<Boolean> copyAndReplaceAsync(String destination, int database) {
        return delegate.copyAndReplaceAsync(destination, database);
    }

    interface PermitSemaphoreAcquire {
        String run();
    }

    interface PermitsSemaphoreAcquire {
        List<String> run();
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

    private List<String> observeAcquiringPermitsSemaphore(boolean tryAcquire, long waitTime, long leaseTime, TimeUnit unit, PermitsSemaphoreAcquire permitsSemaphoreAcquire) {
        RPermitSemaphoreAcquiredContext rPermitSemaphoreAcquiredContext = new RPermitSemaphoreAcquiredContext(
                getName(), tryAcquire, waitTime, leaseTime, unit
        );
        Observation observation = RPermitSemaphoreObservationDocumentation.SEMAPHORE_ACQUIRE.observation(
                null, RPermitSemaphoreAcquiredObservationConvention.DEFAULT,
                () -> rPermitSemaphoreAcquiredContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            List<String> result = permitsSemaphoreAcquire.run();
            rPermitSemaphoreAcquiredContext.setPermitId(result.toString());
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
    public List<String> acquire(int permits) throws InterruptedException {
        try {
            return observeAcquiringPermitsSemaphore(false, -1L, -1L, TimeUnit.SECONDS, () -> {
                try {
                    return delegate.acquire(permits);
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
        try {
            return observeAcquiringPermitSemaphore(false, -1L, leaseTime, unit, () -> {
                try {
                    return delegate.acquire(leaseTime, unit);
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
    public List<String> acquire(int permits, long leaseTime, TimeUnit unit) throws InterruptedException {
        try {
            return observeAcquiringPermitsSemaphore(
                    true, -1, leaseTime, unit, () -> {
                        try {
                            return delegate.acquire(permits, leaseTime, unit);
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
    public String tryAcquire() {
        return observeAcquiringPermitSemaphore(
                true, -1L, -1L, TimeUnit.SECONDS, delegate::tryAcquire
        );
    }

    @Override
    public List<String> tryAcquire(int permits) {
        return observeAcquiringPermitsSemaphore(
                true, -1L, -1L, TimeUnit.SECONDS, () -> delegate.tryAcquire(permits)
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
    public List<String> tryAcquire(int permits, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        try {
            return observeAcquiringPermitsSemaphore(
                    true, waitTime, leaseTime, unit, () -> {
                        try {
                            return delegate.tryAcquire(permits, waitTime, leaseTime, unit);
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
    public int tryRelease(List<String> permitsIds) {
        RPermitSemaphoreReleasedContext rPermitSemaphoreReleasedContext = new RPermitSemaphoreReleasedContext(
                getName(), permitsIds.toString()
        );
        Observation observation = RPermitSemaphoreObservationDocumentation.SEMAPHORE_RELEASE.observation(
                null, RPermitSemaphoreReleasedObservationConvention.DEFAULT,
                () -> rPermitSemaphoreReleasedContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            int result = delegate.tryRelease(permitsIds);
            rPermitSemaphoreReleasedContext.setPermitReleasedSuccessfully(Objects.equals(permitsIds.size(), result));
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
    public void release(List<String> permitsIds) {
        RPermitSemaphoreReleasedContext rPermitSemaphoreReleasedContext = new RPermitSemaphoreReleasedContext(
                getName(), permitsIds.toString()
        );
        Observation observation = RPermitSemaphoreObservationDocumentation.SEMAPHORE_RELEASE.observation(
                null, RPermitSemaphoreReleasedObservationConvention.DEFAULT,
                () -> rPermitSemaphoreReleasedContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            delegate.release(permitsIds);
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
    public int getPermits() {
        return delegate.getPermits();
    }

    @Override
    public int acquiredPermits() {
        return delegate.acquiredPermits();
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
    public void setPermits(int permits) {
        delegate.setPermits(permits);
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
    public RFuture<List<String>> acquireAsync(int permits) {
        return delegate.acquireAsync(permits);
    }

    @Override
    public RFuture<String> acquireAsync(long leaseTime, TimeUnit unit) {
        return delegate.acquireAsync(leaseTime, unit);
    }

    @Override
    public RFuture<List<String>> acquireAsync(int permits, long leaseTime, TimeUnit unit) {
        return delegate.acquireAsync(permits,leaseTime,unit);
    }

    @Override
    public RFuture<String> tryAcquireAsync() {
        return delegate.tryAcquireAsync();
    }

    @Override
    public RFuture<List<String>> tryAcquireAsync(int permits) {
        return delegate.tryAcquireAsync(permits);
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
    public RFuture<List<String>> tryAcquireAsync(int permits, long waitTime, long leaseTime, TimeUnit unit) {
        return delegate.tryAcquireAsync(permits,waitTime,leaseTime,unit);
    }

    @Override
    public RFuture<Boolean> tryReleaseAsync(String permitId) {
        return delegate.tryReleaseAsync(permitId);
    }

    @Override
    public RFuture<Integer> tryReleaseAsync(List<String> permitsIds) {
        return delegate.tryReleaseAsync(permitsIds);
    }

    @Override
    public RFuture<Void> releaseAsync(String permitId) {
        return delegate.releaseAsync(permitId);
    }

    @Override
    public RFuture<Void> releaseAsync(List<String> permitsIds) {
        return delegate.releaseAsync(permitsIds);
    }

    @Override
    public RFuture<Integer> availablePermitsAsync() {
        return delegate.availablePermitsAsync();
    }

    @Override
    public RFuture<Integer> getPermitsAsync() {
        return delegate.getPermitsAsync();
    }

    @Override
    public RFuture<Integer> acquiredPermitsAsync() {
        return delegate.acquiredPermitsAsync();
    }

    @Override
    public RFuture<Boolean> trySetPermitsAsync(int permits) {
        return delegate.trySetPermitsAsync(permits);
    }

    @Override
    public RFuture<Void> setPermitsAsync(int permits) {
        return delegate.setPermitsAsync(permits);
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
