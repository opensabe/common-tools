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

public class ObservedRExpirable extends RObjectDelegate implements RExpirable {
    private final RExpirable rExpirable;
    protected final UnifiedObservationFactory unifiedObservationFactory;
    public ObservedRExpirable(RExpirable rExpirable, UnifiedObservationFactory unifiedObservationFactory) {
        super(rExpirable);
        this.rExpirable = rExpirable;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    private interface ExpireCallable {
        boolean expire();
    }

    private boolean expire0(String expire, ExpireCallable callable) {
        RExpirableExpireContext context = new RExpirableExpireContext(
                rExpirable.getName(), Thread.currentThread().getName(), expire
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
                () -> rExpirable.expire(timeToLive, timeUnit)
        );
    }

    @Override
    public boolean expireAt(long timestamp) {
        return expire0(
                "expireAt timestamp: " + timestamp,
                () -> rExpirable.expireAt(timestamp)
        );
    }

    @Override
    public boolean expireAt(Date timestamp) {
        return expire0(
                "expireAt timestamp: " + timestamp,
                () -> rExpirable.expireAt(timestamp)
        );
    }

    @Override
    public boolean expire(Instant time) {
        return expire0(
                "expire time: " + time,
                () -> rExpirable.expire(time)
        );
    }

    @Override
    public boolean expireIfSet(Instant time) {
        return expire0(
                "expireIfSet time: " + time,
                () -> rExpirable.expireIfSet(time)
        );
    }

    @Override
    public boolean expireIfNotSet(Instant time) {
        return expire0(
                "expireIfNotSet time: " + time,
                () -> rExpirable.expireIfNotSet(time)
        );
    }

    @Override
    public boolean expireIfGreater(Instant time) {
        return expire0(
                "expireIfGreater time: " + time,
                () -> rExpirable.expireIfGreater(time)
        );
    }

    @Override
    public boolean expireIfLess(Instant time) {
        return expire0(
                "expireIfLess time: " + time,
                () -> rExpirable.expireIfLess(time)
        );
    }

    @Override
    public boolean expire(Duration duration) {
        return expire0(
                "expire duration: " + duration,
                () -> rExpirable.expire(duration)
        );
    }

    @Override
    public boolean expireIfSet(Duration duration) {
        return expire0(
                "expireIfSet duration: " + duration,
                () -> rExpirable.expireIfSet(duration)
        );
    }

    @Override
    public boolean expireIfNotSet(Duration duration) {
        return expire0(
                "expireIfNotSet duration: " + duration,
                () -> rExpirable.expireIfNotSet(duration)
        );
    }

    @Override
    public boolean expireIfGreater(Duration duration) {
        return expire0(
                "expireIfGreater duration: " + duration,
                () -> rExpirable.expireIfGreater(duration)
        );
    }

    @Override
    public boolean expireIfLess(Duration duration) {
        return expire0(
                "expireIfLess duration: " + duration,
                () -> rExpirable.expireIfLess(duration)
        );
    }

    @Override
    public boolean clearExpire() {
        return expire0(
                "clearExpire",
                rExpirable::clearExpire
        );
    }

    @Override
    public long remainTimeToLive() {
        return rExpirable.remainTimeToLive();
    }

    @Override
    public long getExpireTime() {
        return rExpirable.getExpireTime();
    }

    @Override
    public RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit) {
        return rExpirable.expireAsync(timeToLive, timeUnit);
    }

    @Override
    public RFuture<Boolean> expireAtAsync(Date timestamp) {
        return rExpirable.expireAtAsync(timestamp);
    }

    @Override
    public RFuture<Boolean> expireAtAsync(long timestamp) {
        return rExpirable.expireAtAsync(timestamp);
    }

    @Override
    public RFuture<Boolean> expireAsync(Instant time) {
        return rExpirable.expireAsync(time);
    }

    @Override
    public RFuture<Boolean> expireIfSetAsync(Instant time) {
        return rExpirable.expireIfSetAsync(time);
    }

    @Override
    public RFuture<Boolean> expireIfNotSetAsync(Instant time) {
        return rExpirable.expireIfNotSetAsync(time);
    }

    @Override
    public RFuture<Boolean> expireIfGreaterAsync(Instant time) {
        return rExpirable.expireIfGreaterAsync(time);
    }

    @Override
    public RFuture<Boolean> expireIfLessAsync(Instant time) {
        return rExpirable.expireIfLessAsync(time);
    }

    @Override
    public RFuture<Boolean> expireAsync(Duration duration) {
        return rExpirable.expireAsync(duration);
    }

    @Override
    public RFuture<Boolean> expireIfSetAsync(Duration duration) {
        return rExpirable.expireIfSetAsync(duration);
    }

    @Override
    public RFuture<Boolean> expireIfNotSetAsync(Duration duration) {
        return rExpirable.expireIfNotSetAsync(duration);
    }

    @Override
    public RFuture<Boolean> expireIfGreaterAsync(Duration duration) {
        return rExpirable.expireIfGreaterAsync(duration);
    }

    @Override
    public RFuture<Boolean> expireIfLessAsync(Duration duration) {
        return rExpirable.expireIfLessAsync(duration);
    }

    @Override
    public RFuture<Boolean> clearExpireAsync() {
        return rExpirable.clearExpireAsync();
    }

    @Override
    public RFuture<Long> remainTimeToLiveAsync() {
        return rExpirable.remainTimeToLiveAsync();
    }

    @Override
    public RFuture<Long> getExpireTimeAsync() {
        return rExpirable.getExpireTimeAsync();
    }
}
