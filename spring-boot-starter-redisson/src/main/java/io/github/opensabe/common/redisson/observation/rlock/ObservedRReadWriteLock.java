/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.redisson.observation.rlock;

import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.springframework.lang.NonNull;

import io.github.opensabe.common.observation.UnifiedObservationFactory;

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
