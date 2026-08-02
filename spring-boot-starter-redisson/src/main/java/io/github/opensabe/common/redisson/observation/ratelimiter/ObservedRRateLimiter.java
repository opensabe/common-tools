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
package io.github.opensabe.common.redisson.observation.ratelimiter;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RFuture;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateLimiterConfig;
import org.redisson.api.RateType;
import org.redisson.api.ratelimiter.RateLimiterArgs;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.observation.rexpirable.ObservedRExpirable;
import io.micrometer.observation.Observation;

/**
 * 带 Micrometer 观测的 {@link RRateLimiter} 装饰器。
 * <p>
 * 同步与异步的 set/update/acquire/release 均在 observation 中记录；异步路径在 {@code whenComplete} 中收尾。
 */
public class ObservedRRateLimiter extends ObservedRExpirable<RRateLimiter> implements RRateLimiter {

    /**
     * @param delegate 底层限流器
     * @param unifiedObservationFactory 统一观测工厂
     */
    public ObservedRRateLimiter(RRateLimiter delegate, UnifiedObservationFactory unifiedObservationFactory) {
        super(delegate, unifiedObservationFactory);
    }
    /**
     * 尝试设置限流速率并记录 SET_RATE observation。
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean trySetRate(RateType mode, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(delegate.getName(), Thread.currentThread().getName(), mode, rate, rateInterval, rateIntervalUnit);
        Observation observation = RRateLimiterObservationDocumentation.SET_RATE.start(
                null,
                RRateLimiterSetRateConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        );
        try {
            boolean result = delegate.trySetRate(mode, rate, rateInterval, rateIntervalUnit);
            context.setSetRateSuccessfully(result);
            return result;
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    /**
     * 尝试设置限流速率并记录 SET_RATE observation。
     */
    @Override
    public boolean trySetRate(RateType mode, long rate, Duration rateInterval) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(delegate.getName(), Thread.currentThread().getName(), mode, rate, rateInterval, Duration.ZERO);
        Observation observation = RRateLimiterObservationDocumentation.SET_RATE.start(
                null,
                RRateLimiterSetRateConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        );
        try {
            boolean result = delegate.trySetRate(mode, rate, rateInterval);
            context.setSetRateSuccessfully(result);
            return result;
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    /**
     * 尝试设置限流速率并记录 SET_RATE observation。
     */
    @Override
    public boolean trySetRate(RateType mode, long rate, Duration rateInterval, Duration keepAliveTime) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(delegate.getName(), Thread.currentThread().getName(), mode, rate, rateInterval, keepAliveTime);
        Observation observation = RRateLimiterObservationDocumentation.SET_RATE.start(
                null,
                RRateLimiterSetRateConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        );
        try {
            boolean result = delegate.trySetRate(mode, rate, rateInterval, keepAliveTime);
            context.setSetRateSuccessfully(result);
            return result;
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    /**
     * 设置限流速率并记录 SET_RATE observation。
     */
    @Override
    @SuppressWarnings("deprecation")
    public void setRate(RateType mode, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(delegate.getName(), Thread.currentThread().getName(), mode, rate, rateInterval, rateIntervalUnit);
        Observation observation = RRateLimiterObservationDocumentation.SET_RATE.start(
                null,
                RRateLimiterSetRateConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        );
        try {
            delegate.setRate(mode, rate, rateInterval, rateIntervalUnit);
            context.setSetRateSuccessfully(true);
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    /**
     * 设置限流速率并记录 SET_RATE observation。
     */
    @Override
    public void setRate(RateType mode, long rate, Duration rateInterval) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(delegate.getName(), Thread.currentThread().getName(), mode, rate, rateInterval, Duration.ZERO);
        Observation observation = RRateLimiterObservationDocumentation.SET_RATE.start(
                null,
                RRateLimiterSetRateConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        );
        try {
            delegate.setRate(mode, rate, rateInterval);
            context.setSetRateSuccessfully(true);
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    /**
     * 设置限流速率并记录 SET_RATE observation。
     */
    @Override
    public void setRate(RateType mode, long rate, Duration rateInterval, Duration keepAliveTime) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(delegate.getName(), Thread.currentThread().getName(), mode, rate, rateInterval, keepAliveTime);
        Observation observation = RRateLimiterObservationDocumentation.SET_RATE.start(
                null,
                RRateLimiterSetRateConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        );
        try {
            delegate.setRate(mode, rate, rateInterval, keepAliveTime);
            context.setSetRateSuccessfully(true);
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    /**
     * 设置限流速率并记录 SET_RATE observation。
     */
    @Override
    public void setRate(RateLimiterArgs args) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(delegate.getName(), Thread.currentThread().getName(), RateType.OVERALL, -1L, Duration.ZERO, Duration.ZERO);
        Observation observation = RRateLimiterObservationDocumentation.SET_RATE.start(
                null,
                RRateLimiterSetRateConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        );
        try {
            delegate.setRate(args);
            context.setSetRateSuccessfully(true);
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    /**
     * 更新限流速率并记录 SET_RATE observation。
     */
    @Override
    public boolean updateRate(RateLimiterArgs args) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(delegate.getName(), Thread.currentThread().getName(), RateType.OVERALL, -1L, Duration.ZERO, Duration.ZERO);
        Observation observation = RRateLimiterObservationDocumentation.SET_RATE.start(
                null,
                RRateLimiterSetRateConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        );
        try {
            boolean result = delegate.updateRate(args);
            context.setSetRateSuccessfully(result);
            return result;
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    /**
     * 同步获取许可的通用观测包装逻辑。
     */
    private boolean acquire0(long permits, long timeout, TimeUnit unit, AcquireCallable callable) {
        RRateLimiterAcquireContext context = new RRateLimiterAcquireContext(
                delegate.getName(), Thread.currentThread().getName(), permits, timeout, unit
        );
        Observation observation = RRateLimiterObservationDocumentation.ACQUIRE.start(
                null,
                RRateLimiterAcquireConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        );
        try {
            boolean result = callable.acquire();
            context.setRateLimiterAcquiredSuccessfully(result);
            return result;
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    /**
     * 尝试获取许可并记录 ACQUIRE observation。
     */
    @Override
    public boolean tryAcquire() {
        return acquire0(1, -1, TimeUnit.SECONDS, delegate::tryAcquire);
    }

    /**
     * 尝试获取许可并记录 ACQUIRE observation。
     */
    @Override
    public boolean tryAcquire(long permits) {
        return acquire0(permits, -1, TimeUnit.SECONDS, () -> delegate.tryAcquire(permits));
    }

    /**
     * 阻塞获取许可并记录 ACQUIRE observation。
     */
    @Override
    public void acquire() {
        acquire0(1, -1, TimeUnit.SECONDS, () -> {
            delegate.acquire();
            return true;
        });
    }

    /**
     * 阻塞获取许可并记录 ACQUIRE observation。
     */
    @Override
    public void acquire(long permits) {
        acquire0(permits, -1, TimeUnit.SECONDS, () -> {
            delegate.acquire(permits);
            return true;
        });
    }

    /**
     * 尝试获取许可并记录 ACQUIRE observation。
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean tryAcquire(long timeout, TimeUnit unit) {
        return acquire0(1, timeout, unit, () -> delegate.tryAcquire(timeout, unit));
    }

    /**
     * 尝试获取许可并记录 ACQUIRE observation。
     */
    @Override
    public boolean tryAcquire(Duration timeout) {
        if (Objects.isNull(timeout)) {
            timeout = Duration.ZERO;
        }
        Duration finalTimeout = timeout;
        return acquire0(1, timeout.toMillis(), TimeUnit.MILLISECONDS, () -> delegate.tryAcquire(finalTimeout));
    }

    /**
     * 尝试获取许可并记录 ACQUIRE observation。
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean tryAcquire(long permits, long timeout, TimeUnit unit) {
        return acquire0(permits, timeout, unit, () -> delegate.tryAcquire(permits, timeout, unit));
    }

    /**
     * 尝试获取许可并记录 ACQUIRE observation。
     */
    @Override
    public boolean tryAcquire(long permits, Duration timeout) {
        return acquire0(permits, timeout.toMillis(), TimeUnit.MILLISECONDS, () -> delegate.tryAcquire(permits, timeout));
    }

    /**
     * 返回底层限流器配置。
     */
    @Override
    public RateLimiterConfig getConfig() {
        return delegate.getConfig();
    }

    /**
     * 返回当前可用许可数。
     */
    @Override
    public long availablePermits() {
        return delegate.availablePermits();
    }

    /**
     * 释放许可；复用 ACQUIRE observation 类型以保持可见性。
     */
    @Override
    public void release(long permits) {
        // No dedicated release JFR event; reuse ACQUIRE observation for visibility.
        RRateLimiterAcquireContext context = new RRateLimiterAcquireContext(
                delegate.getName(), Thread.currentThread().getName(), permits, -1, TimeUnit.SECONDS
        );
        Observation observation = RRateLimiterObservationDocumentation.ACQUIRE.start(
                null,
                RRateLimiterAcquireConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        );
        try {
            delegate.release(permits);
            context.setRateLimiterAcquiredSuccessfully(true);
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    /**
     * 异步尝试设置限流速率并记录 SET_RATE observation。
     */
    @Override
    @SuppressWarnings("deprecation")
    public RFuture<Boolean> trySetRateAsync(RateType mode, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(
                delegate.getName(), Thread.currentThread().getName(), mode, rate, rateInterval, rateIntervalUnit);
        return observeSetRateAsync(context, delegate.trySetRateAsync(mode, rate, rateInterval, rateIntervalUnit));
    }

    /**
     * 异步尝试设置限流速率并记录 SET_RATE observation。
     */
    @Override
    public RFuture<Boolean> trySetRateAsync(RateType mode, long rate, Duration rateInterval) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(
                delegate.getName(), Thread.currentThread().getName(), mode, rate, rateInterval, Duration.ZERO);
        return observeSetRateAsync(context, delegate.trySetRateAsync(mode, rate, rateInterval));
    }

    /**
     * 异步尝试设置限流速率并记录 SET_RATE observation。
     */
    @Override
    public RFuture<Boolean> trySetRateAsync(RateType mode, long rate, Duration rateInterval, Duration keepAliveTime) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(
                delegate.getName(), Thread.currentThread().getName(), mode, rate, rateInterval, keepAliveTime);
        return observeSetRateAsync(context, delegate.trySetRateAsync(mode, rate, rateInterval, keepAliveTime));
    }

    /**
     * 异步尝试获取许可并记录 ACQUIRE observation。
     */
    @Override
    public RFuture<Boolean> tryAcquireAsync() {
        return observeAcquireAsync(1, -1, TimeUnit.SECONDS, delegate.tryAcquireAsync());
    }

    /**
     * 异步尝试获取许可并记录 ACQUIRE observation。
     */
    @Override
    public RFuture<Boolean> tryAcquireAsync(long permits) {
        return observeAcquireAsync(permits, -1, TimeUnit.SECONDS, delegate.tryAcquireAsync(permits));
    }

    /**
     * 异步阻塞获取许可并记录 ACQUIRE observation。
     */
    @Override
    public RFuture<Void> acquireAsync() {
        return observeAcquireAsync(1, -1, TimeUnit.SECONDS, delegate.acquireAsync());
    }

    /**
     * 异步阻塞获取许可并记录 ACQUIRE observation。
     */
    @Override
    public RFuture<Void> acquireAsync(long permits) {
        return observeAcquireAsync(permits, -1, TimeUnit.SECONDS, delegate.acquireAsync(permits));
    }

    /**
     * 异步尝试获取许可并记录 ACQUIRE observation。
     */
    @Override
    @SuppressWarnings("deprecation")
    public RFuture<Boolean> tryAcquireAsync(long timeout, TimeUnit unit) {
        return observeAcquireAsync(1, timeout, unit, delegate.tryAcquireAsync(timeout, unit));
    }

    /**
     * 异步尝试获取许可并记录 ACQUIRE observation。
     */
    @Override
    public RFuture<Boolean> tryAcquireAsync(Duration timeout) {
        return observeAcquireAsync(1, timeout.toMillis(), TimeUnit.MILLISECONDS, delegate.tryAcquireAsync(timeout));
    }

    /**
     * 异步尝试获取许可并记录 ACQUIRE observation。
     */
    @Override
    @SuppressWarnings("deprecation")
    public RFuture<Boolean> tryAcquireAsync(long permits, long timeout, TimeUnit unit) {
        return observeAcquireAsync(permits, timeout, unit, delegate.tryAcquireAsync(permits, timeout, unit));
    }

    /**
     * 异步尝试获取许可并记录 ACQUIRE observation。
     */
    @Override
    public RFuture<Boolean> tryAcquireAsync(long permits, Duration timeout) {
        return observeAcquireAsync(permits, timeout.toMillis(), TimeUnit.MILLISECONDS, delegate.tryAcquireAsync(permits, timeout));
    }

    /**
     * 异步设置限流速率并记录 SET_RATE observation。
     */
    @Override
    @SuppressWarnings("deprecation")
    public RFuture<Void> setRateAsync(RateType mode, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(
                delegate.getName(), Thread.currentThread().getName(), mode, rate, rateInterval, rateIntervalUnit);
        return observeSetRateAsync(context, delegate.setRateAsync(mode, rate, rateInterval, rateIntervalUnit));
    }

    /**
     * 异步设置限流速率并记录 SET_RATE observation。
     */
    @Override
    public RFuture<Void> setRateAsync(RateType mode, long rate, Duration rateInterval) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(
                delegate.getName(), Thread.currentThread().getName(), mode, rate, rateInterval, Duration.ZERO);
        return observeSetRateAsync(context, delegate.setRateAsync(mode, rate, rateInterval));
    }

    /**
     * 异步设置限流速率并记录 SET_RATE observation。
     */
    @Override
    public RFuture<Void> setRateAsync(RateType mode, long rate, Duration rateInterval, Duration keepAliveTime) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(
                delegate.getName(), Thread.currentThread().getName(), mode, rate, rateInterval, keepAliveTime);
        return observeSetRateAsync(context, delegate.setRateAsync(mode, rate, rateInterval, keepAliveTime));
    }

    /**
     * 异步释放许可并记录 ACQUIRE observation。
     */
    @Override
    public RFuture<Void> releaseAsync(long permits) {
        return observeAcquireAsync(permits, -1, TimeUnit.SECONDS, delegate.releaseAsync(permits));
    }

    /**
     * 异步设置限流速率并记录 SET_RATE observation。
     */
    @Override
    public RFuture<Void> setRateAsync(RateLimiterArgs args) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(
                delegate.getName(), Thread.currentThread().getName(), RateType.OVERALL, -1L, Duration.ZERO, Duration.ZERO);
        return observeSetRateAsync(context, delegate.setRateAsync(args));
    }

    /**
     * 异步更新限流速率并记录 SET_RATE observation。
     */
    @Override
    public RFuture<Boolean> updateRateAsync(RateLimiterArgs args) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(
                delegate.getName(), Thread.currentThread().getName(), RateType.OVERALL, -1L, Duration.ZERO, Duration.ZERO);
        return observeSetRateAsync(context, delegate.updateRateAsync(args));
    }

    /**
     * 异步返回底层限流器配置。
     */
    @Override
    public RFuture<RateLimiterConfig> getConfigAsync() {
        return delegate.getConfigAsync();
    }

    /**
     * 异步返回当前可用许可数。
     */
    @Override
    public RFuture<Long> availablePermitsAsync() {
        return delegate.availablePermitsAsync();
    }

    /**
     * 在异步 future 完成时收尾 SET_RATE observation。
     */
    private <T> RFuture<T> observeSetRateAsync(RRateLimiterSetRateContext context, RFuture<T> future) {
        Observation observation = RRateLimiterObservationDocumentation.SET_RATE.start(
                null,
                RRateLimiterSetRateConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        );
        future.whenComplete((result, error) -> {
            try {
                if (error != null) {
                    observation.error(error);
                } else if (result instanceof Boolean success) {
                    context.setSetRateSuccessfully(success);
                } else {
                    context.setSetRateSuccessfully(true);
                }
            } finally {
                observation.stop();
            }
        });
        return future;
    }

    /**
     * 在异步 future 完成时收尾 ACQUIRE observation。
     */
    private <T> RFuture<T> observeAcquireAsync(long permits, long timeout, TimeUnit unit, RFuture<T> future) {
        RRateLimiterAcquireContext context = new RRateLimiterAcquireContext(
                delegate.getName(), Thread.currentThread().getName(), permits, timeout, unit
        );
        Observation observation = RRateLimiterObservationDocumentation.ACQUIRE.start(
                null,
                RRateLimiterAcquireConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        );
        future.whenComplete((result, error) -> {
            try {
                if (error != null) {
                    observation.error(error);
                } else if (result instanceof Boolean success) {
                    context.setRateLimiterAcquiredSuccessfully(success);
                } else {
                    context.setRateLimiterAcquiredSuccessfully(true);
                }
            } finally {
                observation.stop();
            }
        });
        return future;
    }

    /**
     * 限流器操作：None。
     */
    /**
     * 同步 acquire 操作的函数式回调。
     */
    private interface AcquireCallable {
        boolean acquire();
    }
}
