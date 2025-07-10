package io.github.opensabe.common.redisson.observation.rexpirable;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.observation.RObjectDelegate;
import io.micrometer.observation.Observation;
import org.redisson.api.RExpirable;
import org.redisson.api.RFuture;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ObservedRExpirable<T extends RExpirable> extends RObjectDelegate<T> implements RExpirable {
    protected final UnifiedObservationFactory unifiedObservationFactory;
    public ObservedRExpirable(T rExpirable, UnifiedObservationFactory unifiedObservationFactory) {
        super(rExpirable);
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    private interface ExpireCallable {
        boolean expire();
    }

    private boolean expire0(String expire, ExpireCallable callable) {
        RExpirableExpireContext context = new RExpirableExpireContext(
                delegate.getName(), Thread.currentThread().getName(), expire
        );
        Observation observation = RExpirableObservationDocumentation.EXPIRE.start(
                null,
                RExpirableExpireConvention.DEFAULT,
                () -> context,
                unifiedObservationFactory.getObservationRegistry()
        );
        try {
            boolean result = callable.expire();
            context.setExpireSetSuccessfully(result);
            return result;
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }

    @Override
    public boolean expire(long timeToLive, TimeUnit timeUnit) {
        return expire0(
                "expire timeToLive: " + timeToLive + " " + timeUnit.name().toLowerCase(),
                () -> delegate.expire(timeToLive, timeUnit)
        );
    }

    @Override
    public boolean expireAt(long timestamp) {
        return expire0(
                "expireAt timestamp: " + timestamp,
                () -> delegate.expireAt(timestamp)
        );
    }

    @Override
    public boolean expireAt(Date timestamp) {
        return expire0(
                "expireAt timestamp: " + timestamp,
                () -> delegate.expireAt(timestamp)
        );
    }

    @Override
    public boolean expire(Instant time) {
        return expire0(
                "expire time: " + time,
                () -> delegate.expire(time)
        );
    }

    @Override
    public boolean expireIfSet(Instant time) {
        return expire0(
                "expireIfSet time: " + time,
                () -> delegate.expireIfSet(time)
        );
    }

    @Override
    public boolean expireIfNotSet(Instant time) {
        return expire0(
                "expireIfNotSet time: " + time,
                () -> delegate.expireIfNotSet(time)
        );
    }

    @Override
    public boolean expireIfGreater(Instant time) {
        return expire0(
                "expireIfGreater time: " + time,
                () -> delegate.expireIfGreater(time)
        );
    }

    @Override
    public boolean expireIfLess(Instant time) {
        return expire0(
                "expireIfLess time: " + time,
                () -> delegate.expireIfLess(time)
        );
    }

    @Override
    public boolean expire(Duration duration) {
        return expire0(
                "expire duration: " + duration,
                () -> delegate.expire(duration)
        );
    }

    @Override
    public boolean expireIfSet(Duration duration) {
        return expire0(
                "expireIfSet duration: " + duration,
                () -> delegate.expireIfSet(duration)
        );
    }

    @Override
    public boolean expireIfNotSet(Duration duration) {
        return expire0(
                "expireIfNotSet duration: " + duration,
                () -> delegate.expireIfNotSet(duration)
        );
    }

    @Override
    public boolean expireIfGreater(Duration duration) {
        return expire0(
                "expireIfGreater duration: " + duration,
                () -> delegate.expireIfGreater(duration)
        );
    }

    @Override
    public boolean expireIfLess(Duration duration) {
        return expire0(
                "expireIfLess duration: " + duration,
                () -> delegate.expireIfLess(duration)
        );
    }

    @Override
    public boolean clearExpire() {
        return expire0(
                "clearExpire",
                delegate::clearExpire
        );
    }

    @Override
    public long remainTimeToLive() {
        return delegate.remainTimeToLive();
    }

    @Override
    public long getExpireTime() {
        return delegate.getExpireTime();
    }

    @Override
    public RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit) {
        return delegate.expireAsync(timeToLive, timeUnit);
    }

    @Override
    public RFuture<Boolean> expireAtAsync(Date timestamp) {
        return delegate.expireAtAsync(timestamp);
    }

    @Override
    public RFuture<Boolean> expireAtAsync(long timestamp) {
        return delegate.expireAtAsync(timestamp);
    }

    @Override
    public RFuture<Boolean> expireAsync(Instant time) {
        return delegate.expireAsync(time);
    }

    @Override
    public RFuture<Boolean> expireIfSetAsync(Instant time) {
        return delegate.expireIfSetAsync(time);
    }

    @Override
    public RFuture<Boolean> expireIfNotSetAsync(Instant time) {
        return delegate.expireIfNotSetAsync(time);
    }

    @Override
    public RFuture<Boolean> expireIfGreaterAsync(Instant time) {
        return delegate.expireIfGreaterAsync(time);
    }

    @Override
    public RFuture<Boolean> expireIfLessAsync(Instant time) {
        return delegate.expireIfLessAsync(time);
    }

    @Override
    public RFuture<Boolean> expireAsync(Duration duration) {
        return delegate.expireAsync(duration);
    }

    @Override
    public RFuture<Boolean> expireIfSetAsync(Duration duration) {
        return delegate.expireIfSetAsync(duration);
    }

    @Override
    public RFuture<Boolean> expireIfNotSetAsync(Duration duration) {
        return delegate.expireIfNotSetAsync(duration);
    }

    @Override
    public RFuture<Boolean> expireIfGreaterAsync(Duration duration) {
        return delegate.expireIfGreaterAsync(duration);
    }

    @Override
    public RFuture<Boolean> expireIfLessAsync(Duration duration) {
        return delegate.expireIfLessAsync(duration);
    }

    @Override
    public RFuture<Boolean> clearExpireAsync() {
        return delegate.clearExpireAsync();
    }

    @Override
    public RFuture<Long> remainTimeToLiveAsync() {
        return delegate.remainTimeToLiveAsync();
    }

    @Override
    public RFuture<Long> getExpireTimeAsync() {
        return delegate.getExpireTimeAsync();
    }
}
