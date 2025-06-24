package io.github.opensabe.common.redisson.observation.rlock;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.springframework.lang.NonNull;

public class ObservedRReadWriteLock implements RReadWriteLock {
    private final RReadWriteLock delegate;
    private final UnifiedObservationFactory unifiedObservationFactory;

    public ObservedRReadWriteLock(RReadWriteLock delegate, UnifiedObservationFactory unifiedObservationFactory) {
        this.delegate = delegate;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    @Override
    @NonNull
    public RLock readLock() {
        return new ObservedRLock<>(delegate.readLock(), unifiedObservationFactory);
    }

    @Override
    @NonNull
    public RLock writeLock() {
        return new ObservedRLock<>(delegate.writeLock(), unifiedObservationFactory);
    }
}
