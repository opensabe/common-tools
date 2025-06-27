package io.github.opensabe.common.redisson.observation;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.observation.ratelimiter.ObservedRRateLimiter;
import io.github.opensabe.common.redisson.observation.rbucket.ObservedRBucket;
import io.github.opensabe.common.redisson.observation.rlock.ObservedRFencedLock;
import io.github.opensabe.common.redisson.observation.rlock.ObservedRLock;
import io.github.opensabe.common.redisson.observation.rlock.ObservedRReadWriteLock;
import io.github.opensabe.common.redisson.observation.rsemaphore.ObservedRPermitExpirableSemaphore;
import org.redisson.api.*;
import org.redisson.api.options.CommonOptions;

import java.util.Collection;

public class ObservedRedissonClient extends RedissonClientDelegate {
    private final UnifiedObservationFactory unifiedObservationFactory;

    public ObservedRedissonClient(RedissonClient delegate, UnifiedObservationFactory unifiedObservationFactory) {
        super(delegate);
        this.unifiedObservationFactory = unifiedObservationFactory;
    }


    @Override
    public RRateLimiter getRateLimiter(String name) {
        return new ObservedRRateLimiter(
                delegate.getRateLimiter(name), unifiedObservationFactory
        );
    }

    @Override
    public RRateLimiter getRateLimiter(CommonOptions options) {
        return new ObservedRRateLimiter(
                delegate.getRateLimiter(options), unifiedObservationFactory
        );
    }



    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(String name) {
        return new ObservedRPermitExpirableSemaphore(
                delegate.getPermitExpirableSemaphore(name), unifiedObservationFactory
        );
    }

    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(CommonOptions options) {
        return new ObservedRPermitExpirableSemaphore(
                delegate.getPermitExpirableSemaphore(options), unifiedObservationFactory
        );
    }

    private RLock getObservedLock(RLock delegate) {
        return new ObservedRLock<>(delegate, unifiedObservationFactory);
    }

    private RFencedLock getObservedRFencedLock(RFencedLock delegate) {
        return new ObservedRFencedLock(delegate, unifiedObservationFactory);
    }

    @Override
    public RLock getLock(String name) {
        return getObservedLock(delegate.getLock(name));
    }

    @Override
    public RLock getLock(CommonOptions options) {
        return getObservedLock(delegate.getLock(options));
    }

    @Override
    public RLock getSpinLock(String name) {
        return getObservedLock(delegate.getSpinLock(name));
    }

    @Override
    public RLock getSpinLock(String name, LockOptions.BackOff backOff) {
        return getObservedLock(delegate.getSpinLock(name, backOff));
    }

    @Override
    public RFencedLock getFencedLock(String name) {
        return getObservedRFencedLock(delegate.getFencedLock(name));
    }

    @Override
    public RFencedLock getFencedLock(CommonOptions options) {
        return getObservedRFencedLock(delegate.getFencedLock(options));
    }

    @Override
    public RLock getMultiLock(RLock... locks) {
        return getObservedLock(delegate.getMultiLock(locks));
    }

    @Override
    public RLock getMultiLock(String group, Collection<Object> values) {
        return getObservedLock(delegate.getMultiLock(group,values));
    }

    @Override
    @SuppressWarnings("deprecation")
    public RLock getRedLock(RLock... locks) {
        return getObservedLock(delegate.getRedLock(locks));
    }

    @Override
    public RLock getFairLock(String name) {
        return getObservedLock(delegate.getFairLock(name));
    }

    @Override
    public RLock getFairLock(CommonOptions options) {
        return getObservedLock(delegate.getFairLock(options));
    }

    @Override
    public RReadWriteLock getReadWriteLock(String name) {
        return new ObservedRReadWriteLock(delegate.getReadWriteLock(name), unifiedObservationFactory);
    }

    @Override
    public RReadWriteLock getReadWriteLock(CommonOptions options) {
        return new ObservedRReadWriteLock(delegate.getReadWriteLock(options), unifiedObservationFactory);
    }

    @Override
    public <V> RBucket<V> getBucket(String name) {
        return new ObservedRBucket<>(super.getBucket(name), unifiedObservationFactory);
    }
}
