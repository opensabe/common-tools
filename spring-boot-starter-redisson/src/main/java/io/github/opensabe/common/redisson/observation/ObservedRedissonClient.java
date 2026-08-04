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
package io.github.opensabe.common.redisson.observation;

import java.util.Collection;

import org.redisson.api.LockOptions;
import org.redisson.api.RFencedLock;
import org.redisson.api.RLock;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.CommonOptions;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.observation.ratelimiter.ObservedRRateLimiter;
import io.github.opensabe.common.redisson.observation.rlock.ObservedRFencedLock;
import io.github.opensabe.common.redisson.observation.rlock.ObservedRLock;
import io.github.opensabe.common.redisson.observation.rlock.ObservedRReadWriteLock;
import io.github.opensabe.common.redisson.observation.rsemaphore.ObservedRPermitExpirableSemaphore;

/**
 * 带 Micrometer 观测的 {@link RedissonClient} 装饰器。
 * <p>
 * 对限流器、信号量与各类分布式锁返回观测包装实例，其余 API 仍委托底层客户端。
 */
public class ObservedRedissonClient extends RedissonClientDelegate {

    /** 统一观测工厂，用于创建 observation。 */
    private final UnifiedObservationFactory unifiedObservationFactory;

    /**
     * @param delegate 底层 Redisson 客户端
     * @param unifiedObservationFactory 观测工厂
     */
    public ObservedRedissonClient(RedissonClient delegate, UnifiedObservationFactory unifiedObservationFactory) {
        super(delegate);
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    /** {@inheritDoc} — 返回带观测的 {@link ObservedRRateLimiter}。 */
    @Override
    public RRateLimiter getRateLimiter(String name) {
        return new ObservedRRateLimiter(
                delegate.getRateLimiter(name), unifiedObservationFactory
        );
    }

    /** {@inheritDoc} — 返回带观测的 {@link ObservedRRateLimiter}。 */
    @Override
    public RRateLimiter getRateLimiter(CommonOptions options) {
        return new ObservedRRateLimiter(
                delegate.getRateLimiter(options), unifiedObservationFactory
        );
    }

    /** {@inheritDoc} — 返回带观测的可过期信号量。 */
    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(String name) {
        return new ObservedRPermitExpirableSemaphore(
                delegate.getPermitExpirableSemaphore(name), unifiedObservationFactory
        );
    }

    /** {@inheritDoc} — 返回带观测的可过期信号量。 */
    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(CommonOptions options) {
        return new ObservedRPermitExpirableSemaphore(
                delegate.getPermitExpirableSemaphore(options), unifiedObservationFactory
        );
    }

    /**
     * 将底层 {@link RLock} 包装为带观测的 {@link ObservedRLock}。
     *
     * @param delegate 底层锁
     * @return 观测锁实例
     */
    private RLock getObservedLock(RLock delegate) {
        return new ObservedRLock<>(delegate, unifiedObservationFactory);
    }

    /**
     * 将底层 {@link RFencedLock} 包装为带观测的 {@link ObservedRFencedLock}。
     *
     * @param delegate 底层栅栏锁
     * @return 观测栅栏锁实例
     */
    private RFencedLock getObservedRFencedLock(RFencedLock delegate) {
        return new ObservedRFencedLock(delegate, unifiedObservationFactory);
    }

    /** {@inheritDoc} */
    @Override
    public RLock getLock(String name) {
        return getObservedLock(delegate.getLock(name));
    }

    /** {@inheritDoc} */
    @Override
    public RLock getLock(CommonOptions options) {
        return getObservedLock(delegate.getLock(options));
    }

    /** {@inheritDoc} */
    @Override
    public RLock getSpinLock(String name) {
        return getObservedLock(delegate.getSpinLock(name));
    }

    /** {@inheritDoc} */
    @Override
    public RLock getSpinLock(String name, LockOptions.BackOff backOff) {
        return getObservedLock(delegate.getSpinLock(name, backOff));
    }

    /** {@inheritDoc} */
    @Override
    public RFencedLock getFencedLock(String name) {
        return getObservedRFencedLock(delegate.getFencedLock(name));
    }

    /** {@inheritDoc} */
    @Override
    public RFencedLock getFencedLock(CommonOptions options) {
        return getObservedRFencedLock(delegate.getFencedLock(options));
    }

    /** {@inheritDoc} */
    @Override
    public RLock getMultiLock(RLock... locks) {
        return getObservedLock(delegate.getMultiLock(locks));
    }

    /** {@inheritDoc} */
    @Override
    public RLock getMultiLock(String group, Collection<Object> values) {
        return getObservedLock(delegate.getMultiLock(group, values));
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("deprecation")
    public RLock getRedLock(RLock... locks) {
        return getObservedLock(delegate.getRedLock(locks));
    }

    /** {@inheritDoc} */
    @Override
    public RLock getFairLock(String name) {
        return getObservedLock(delegate.getFairLock(name));
    }

    /** {@inheritDoc} */
    @Override
    public RLock getFairLock(CommonOptions options) {
        return getObservedLock(delegate.getFairLock(options));
    }

    /** {@inheritDoc} */
    @Override
    public RLock getNonReentrantLock(String name) {
        return getObservedLock(delegate.getNonReentrantLock(name));
    }

    /** {@inheritDoc} */
    @Override
    public RLock getNonReentrantLock(CommonOptions options) {
        return getObservedLock(delegate.getNonReentrantLock(options));
    }

    /** {@inheritDoc} */
    @Override
    public RLock getNonReentrantFairLock(String name) {
        return getObservedLock(delegate.getNonReentrantFairLock(name));
    }

    /** {@inheritDoc} */
    @Override
    public RLock getNonReentrantFairLock(CommonOptions options) {
        return getObservedLock(delegate.getNonReentrantFairLock(options));
    }

    /** {@inheritDoc} — 返回带观测的读写锁。 */
    @Override
    public RReadWriteLock getReadWriteLock(String name) {
        return new ObservedRReadWriteLock(delegate.getReadWriteLock(name), unifiedObservationFactory);
    }

    /** {@inheritDoc} — 返回带观测的读写锁。 */
    @Override
    public RReadWriteLock getReadWriteLock(CommonOptions options) {
        return new ObservedRReadWriteLock(delegate.getReadWriteLock(options), unifiedObservationFactory);
    }
}
