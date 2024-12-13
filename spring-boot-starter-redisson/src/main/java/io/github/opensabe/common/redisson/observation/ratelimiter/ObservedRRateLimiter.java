package io.github.opensabe.common.redisson.observation.ratelimiter;

import io.github.opensabe.common.redisson.observation.rexpirable.ObservedRExpirable;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import org.redisson.api.RFuture;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateLimiterConfig;
import org.redisson.api.RateType;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class ObservedRRateLimiter extends ObservedRExpirable implements RRateLimiter {
    private final RRateLimiter delegate;
    private final UnifiedObservationFactory unifiedObservationFactory;

    public ObservedRRateLimiter(RRateLimiter delegate, UnifiedObservationFactory unifiedObservationFactory) {
        super(delegate, unifiedObservationFactory);
        this.delegate = delegate;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    @Override
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

    @Override
    public boolean trySetRate(RateType mode, long rate, Duration rateInterval) {
        RRateLimiterSetRateContext context = new RRateLimiterSetRateContext(delegate.getName(), Thread.currentThread().getName(), mode, rate, rateInterval,Duration.ZERO);
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

    @Override
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

    private interface AcquireCallable {
        boolean acquire();
    }

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

    @Override
    public boolean tryAcquire() {
        return acquire0(1, -1, TimeUnit.SECONDS, delegate::tryAcquire);
    }

    @Override
    public boolean tryAcquire(long permits) {
        return acquire0(permits, -1, TimeUnit.SECONDS, () -> delegate.tryAcquire(permits));
    }

    @Override
    public void acquire() {
        acquire0(1, -1, TimeUnit.SECONDS, () -> {
            delegate.acquire();
            return true;
        });
    }

    @Override
    public void acquire(long permits) {
        acquire0(permits, -1, TimeUnit.SECONDS, () -> {
            delegate.acquire(permits);
            return true;
        });
    }

    @Override
    public boolean tryAcquire(long timeout, TimeUnit unit) {
        return acquire0(1, timeout, unit, () -> delegate.tryAcquire(timeout, unit));
    }

    @Override
    public boolean tryAcquire(Duration timeout) {
        return acquire0(1, timeout.toMillis(), TimeUnit.MILLISECONDS, () -> delegate.tryAcquire(timeout));
    }

    @Override
    public boolean tryAcquire(long permits, long timeout, TimeUnit unit) {
        return acquire0(permits, timeout, unit, () -> delegate.tryAcquire(permits, timeout, unit));
    }

    @Override
    public boolean tryAcquire(long permits, Duration timeout) {
        return acquire0(permits, timeout.toMillis(), TimeUnit.MILLISECONDS, () -> delegate.tryAcquire(permits,timeout));
    }

    @Override
    public RateLimiterConfig getConfig() {
        return delegate.getConfig();
    }

    @Override
    public long availablePermits() {
        return delegate.availablePermits();
    }

    @Override
    public RFuture<Boolean> trySetRateAsync(RateType mode, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit) {
        return delegate.trySetRateAsync(mode, rate, rateInterval, rateIntervalUnit);
    }

    @Override
    public RFuture<Boolean> trySetRateAsync(RateType mode, long rate, Duration rateInterval) {
        return delegate.trySetRateAsync(mode, rate, rateInterval);
    }

    @Override
    public RFuture<Boolean> trySetRateAsync(RateType mode, long rate, Duration rateInterval, Duration keepAliveTime) {
        return delegate.trySetRateAsync(mode,rate,rateInterval,keepAliveTime);
    }

    @Override
    public RFuture<Boolean> tryAcquireAsync() {
        return delegate.tryAcquireAsync();
    }

    @Override
    public RFuture<Boolean> tryAcquireAsync(long permits) {
        return delegate.tryAcquireAsync(permits);
    }

    @Override
    public RFuture<Void> acquireAsync() {
        return delegate.acquireAsync();
    }

    @Override
    public RFuture<Void> acquireAsync(long permits) {
        return delegate.acquireAsync(permits);
    }

    @Override
    public RFuture<Boolean> tryAcquireAsync(long timeout, TimeUnit unit) {
        return delegate.tryAcquireAsync(timeout, unit);
    }

    @Override
    public RFuture<Boolean> tryAcquireAsync(Duration timeout) {
        return delegate.tryAcquireAsync(timeout);
    }

    @Override
    public RFuture<Boolean> tryAcquireAsync(long permits, long timeout, TimeUnit unit) {
        return delegate.tryAcquireAsync(permits, timeout, unit);
    }

    @Override
    public RFuture<Boolean> tryAcquireAsync(long permits, Duration timeout) {
        return delegate.tryAcquireAsync(permits,timeout);
    }

    @Override
    public RFuture<Void> setRateAsync(RateType mode, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit) {
        return delegate.setRateAsync(mode, rate, rateInterval, rateIntervalUnit);
    }

    @Override
    public RFuture<Void> setRateAsync(RateType mode, long rate, Duration rateInterval) {
        return delegate.setRateAsync(mode, rate, rateInterval);
    }

    @Override
    public RFuture<Void> setRateAsync(RateType mode, long rate, Duration rateInterval, Duration keepAliveTime) {
        return delegate.setRateAsync(mode, rate, rateInterval, keepAliveTime);
    }

    @Override
    public RFuture<RateLimiterConfig> getConfigAsync() {
        return delegate.getConfigAsync();
    }

    @Override
    public RFuture<Long> availablePermitsAsync() {
        return delegate.availablePermitsAsync();
    }
}
