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

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RFencedLock;
import org.redisson.api.RFuture;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;

/**
 * 观察者模式的分布式锁
 * 注意每次都要新建一个实例，不要重复使用
 * 通过正常的 api 就是每次新建一个，但是注意不要使用单例模式
 */
public class ObservedRFencedLock extends ObservedRLock<RFencedLock> implements RFencedLock {

    public ObservedRFencedLock(RFencedLock delegate, UnifiedObservationFactory unifiedObservationFactory) {
        super(delegate, unifiedObservationFactory);
    }


    @Override
    public Long getToken() {
        return delegate.getToken();
    }

    @Override
    public Long lockAndGetToken() {
        return observeAcquiringLockAndGetToken(-1L, -1L, null, delegate::lockAndGetToken);
    }

    @Override
    public Long lockAndGetToken(long leaseTime, TimeUnit unit) {
        return observeAcquiringLockAndGetToken(-1L, leaseTime, unit, () -> delegate.lockAndGetToken(leaseTime, unit));
    }

    @Override
    public Long tryLockAndGetToken() {
        return observeAcquiringLockAndGetToken(-1L, -1, null, delegate::tryLockAndGetToken);
    }

    @Override
    public Long tryLockAndGetToken(long waitTime, TimeUnit unit) {
        return observeAcquiringLockAndGetToken(waitTime, -1, unit, () -> delegate.tryLockAndGetToken(waitTime, unit));
    }

    @Override
    public Long tryLockAndGetToken(long waitTime, long leaseTime, TimeUnit unit) {
        return observeAcquiringLockAndGetToken(waitTime, leaseTime, unit, () -> delegate.tryLockAndGetToken(waitTime, leaseTime, unit));
    }


    private Long observeAcquiringLockAndGetToken(long waitTime, long leaseTime, TimeUnit unit, Supplier<Long> lockAcquire) {
        RLockAcquiredContext rLockAcquiredContext = new RLockAcquiredContext(
                getName(), false, waitTime, leaseTime, unit, delegate.getClass()
        );
        Observation observation = RLockObservationDocumentation.LOCK_ACQUIRE.observation(
                null, RLockAcquiredObservationConvention.DEFAULT,
                () -> rLockAcquiredContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        try {
            Long token = lockAcquire.get();
            rLockAcquiredContext.setLockAcquiredSuccessfully(Objects.nonNull(token));
            return token;
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }


    @Override
    public RFuture<Long> getTokenAsync() {
        return delegate.getTokenAsync();
    }

    @Override
    public RFuture<Long> lockAndGetTokenAsync() {
        return delegate.lockAndGetTokenAsync();
    }

    @Override
    public RFuture<Long> lockAndGetTokenAsync(long leaseTime, TimeUnit unit) {
        return delegate.lockAndGetTokenAsync(leaseTime, unit);
    }

    @Override
    public RFuture<Long> tryLockAndGetTokenAsync() {
        return delegate.tryLockAndGetTokenAsync();
    }

    @Override
    public RFuture<Long> tryLockAndGetTokenAsync(long waitTime, TimeUnit unit) {
        return delegate.tryLockAndGetTokenAsync(waitTime, unit);
    }

    @Override
    public RFuture<Long> tryLockAndGetTokenAsync(long waitTime, long leaseTime, TimeUnit unit) {
        return delegate.tryLockAndGetTokenAsync(waitTime, leaseTime, unit);
    }
}
