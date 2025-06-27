package io.github.opensabe.common.redisson.observation.rbucket;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.observation.rexpirable.ObservedRExpirable;
import io.micrometer.observation.Observation;
import org.redisson.api.RBucket;
import org.redisson.api.RFuture;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author heng.ma
 */
public class ObservedRBucket<V> extends ObservedRExpirable<RBucket<V>> implements RBucket<V> {

    public ObservedRBucket(RBucket<V> wrapper, UnifiedObservationFactory unifiedObservationFactory) {
        super(wrapper, unifiedObservationFactory);
    }

    @Override
    public long size() {
        return delegate.size();
    }

    @Override
    public V get() {
        RBucketContext context = new RBucketContext(getName(), "get");
        Observation observation = RBucketObservationDocumentation.GET.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(delegate::get);
    }

    @Override
    public V getAndDelete() {
        RBucketContext context = new RBucketContext(getName(), "getAndDelete");
        Observation observation = RBucketObservationDocumentation.DELETE.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(delegate::getAndDelete);
    }

    @Override
    public boolean trySet(V value) {
        return delegate.trySet(value);
    }

    @Override
    public boolean trySet(V value, long timeToLive, TimeUnit timeUnit) {
        return delegate.trySet(value, timeToLive, timeUnit);
    }

    @Override
    public boolean setIfAbsent(V value) {
        RBucketContext context = new RBucketContext(getName(), "setIfAbsent(value)");
        Observation observation = RBucketObservationDocumentation.SET.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.setIfAbsent(value));
    }

    @Override
    public boolean setIfAbsent(V value, Duration duration) {
        RBucketContext context = new RBucketContext(getName(), "setIfAbsent(value,duration)", duration);
        Observation observation = RBucketObservationDocumentation.SET.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.setIfAbsent(value, duration));
    }

    @Override
    public boolean setIfExists(V value) {
        RBucketContext context = new RBucketContext(getName(), "setIfExists(value)");
        Observation observation = RBucketObservationDocumentation.SET.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.setIfExists(value));
    }

    @Override
    public boolean setIfExists(V value, long timeToLive, TimeUnit timeUnit) {
        RBucketContext context = new RBucketContext(getName(), "setIfExists(value,timeToLive,imeUnit)", Duration.of(timeToLive, timeUnit.toChronoUnit()));
        Observation observation = RBucketObservationDocumentation.SET.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.setIfExists(value, timeToLive, timeUnit));
    }

    @Override
    public boolean setIfExists(V value, Duration duration) {
        RBucketContext context = new RBucketContext(getName(), "setIfExists(value, duration)", duration);
        Observation observation = RBucketObservationDocumentation.SET.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.setIfExists(value, duration));
    }

    @Override
    public boolean compareAndSet(V expect, V update) {
        return delegate.compareAndSet(expect, update);
    }

    @Override
    public V getAndSet(V newValue) {
        RBucketContext context = new RBucketContext(getName(), "getAndSet(newValue)");
        Observation observation = RBucketObservationDocumentation.SET.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.getAndSet(newValue));
    }

    @Override
    public V getAndSet(V value, long timeToLive, TimeUnit timeUnit) {
        RBucketContext context = new RBucketContext(getName(), "getAndSet(value,timeToLive,timeUnit)", Duration.of(timeToLive, timeUnit.toChronoUnit()));
        Observation observation = RBucketObservationDocumentation.SET.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.getAndSet(value, timeToLive, timeUnit));
    }

    @Override
    public V getAndSet(V value, Duration duration) {
        RBucketContext context = new RBucketContext(getName(), "getAndSet(value,duration)", duration);
        Observation observation = RBucketObservationDocumentation.SET.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.getAndSet(value, duration));
    }

    @Override
    public V getAndExpire(Duration duration) {
        RBucketContext context = new RBucketContext(getName(), "getAndExpire(duration)", duration);
        Observation observation = RBucketObservationDocumentation.EXPIRE.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.getAndExpire(duration));
    }

    @Override
    public V getAndExpire(Instant time) {
        RBucketContext context = new RBucketContext(getName(), "getAndExpire(time)", time);
        Observation observation = RBucketObservationDocumentation.EXPIRE.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(() -> delegate.getAndExpire(time));
    }

    @Override
    public V getAndClearExpire() {
        RBucketContext context = new RBucketContext(getName(), "getAndClearExpire");
        Observation observation = RBucketObservationDocumentation.EXPIRE.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        return observation.observe(delegate::getAndClearExpire);
    }

    @Override
    public void set(V value) {
        RBucketContext context = new RBucketContext(getName(), "set(value)");
        Observation observation = RBucketObservationDocumentation.SET.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        observation.observe(() -> delegate.set(value));
    }

    @Override
    public void set(V value, long timeToLive, TimeUnit timeUnit) {
        RBucketContext context = new RBucketContext(getName(), "set(value,timeToLive,timeUnit)", Duration.of(timeToLive, timeUnit.toChronoUnit()));
        Observation observation = RBucketObservationDocumentation.SET.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        observation.observe(() -> delegate.set(value, timeToLive, timeUnit));
    }

    @Override
    public void set(V value, Duration duration) {
        RBucketContext context = new RBucketContext(getName(), "set(value,duration)", duration);
        Observation observation = RBucketObservationDocumentation.SET.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        observation.observe(() -> delegate.set(value, duration));
    }

    @Override
    public void setAndKeepTTL(V value) {
        RBucketContext context = new RBucketContext(getName(), "setAndKeepTTL(value)");
        Observation observation = RBucketObservationDocumentation.SET.observation(null, RBucketObservationConvention.DEFAULT,
                () -> context, unifiedObservationFactory.getObservationRegistry());
        observation.observe(() -> delegate.setAndKeepTTL(value));
    }

    @Override
    public V findCommon(String name) {
        return delegate.findCommon(name);
    }

    @Override
    public long findCommonLength(String name) {
        return delegate.findCommonLength(name);
    }

    @Override
    public RFuture<Long> sizeAsync() {
        return delegate.sizeAsync();
    }

    @Override
    public RFuture<V> getAsync() {
        return delegate.getAsync();
    }

    @Override
    public RFuture<V> getAndDeleteAsync() {
        return delegate.getAndDeleteAsync();
    }

    @Override
    public RFuture<Boolean> trySetAsync(V value) {
        return delegate.trySetAsync(value);
    }

    @Override
    public RFuture<Boolean> trySetAsync(V value, long timeToLive, TimeUnit timeUnit) {
        return delegate.trySetAsync(value, timeToLive, timeUnit);
    }

    @Override
    public RFuture<Boolean> setIfAbsentAsync(V value) {
        return delegate.setIfAbsentAsync(value);
    }

    @Override
    public RFuture<Boolean> setIfAbsentAsync(V value, Duration duration) {
        return delegate.setIfAbsentAsync(value, duration);
    }

    @Override
    public RFuture<Boolean> setIfExistsAsync(V value) {
        return delegate.setIfExistsAsync(value);
    }

    @Override
    public RFuture<Boolean> setIfExistsAsync(V value, long timeToLive, TimeUnit timeUnit) {
        return delegate.setIfExistsAsync(value, timeToLive, timeUnit);
    }

    @Override
    public RFuture<Boolean> setIfExistsAsync(V value, Duration duration) {
        return delegate.setIfExistsAsync(value, duration);
    }

    @Override
    public RFuture<Boolean> compareAndSetAsync(V expect, V update) {
        return delegate.compareAndSetAsync(expect, update);
    }

    @Override
    public RFuture<V> getAndSetAsync(V newValue) {
        return delegate.getAndSetAsync(newValue);
    }

    @Override
    public RFuture<V> getAndSetAsync(V value, long timeToLive, TimeUnit timeUnit) {
        return delegate.getAndSetAsync(value, timeToLive, timeUnit);
    }

    @Override
    public RFuture<V> getAndSetAsync(V value, Duration duration) {
        return delegate.getAndSetAsync(value, duration);
    }

    @Override
    public RFuture<V> getAndExpireAsync(Duration duration) {
        return delegate.getAndExpireAsync(duration);
    }

    @Override
    public RFuture<V> getAndExpireAsync(Instant time) {
        return delegate.getAndExpireAsync(time);
    }

    @Override
    public RFuture<V> getAndClearExpireAsync() {
        return delegate.getAndClearExpireAsync();
    }

    @Override
    public RFuture<Void> setAsync(V value) {
        return delegate.setAsync(value);
    }

    @Override
    public RFuture<Void> setAsync(V value, long timeToLive, TimeUnit timeUnit) {
        return delegate.setAsync(value, timeToLive, timeUnit);
    }

    @Override
    public RFuture<Void> setAsync(V value, Duration duration) {
        return delegate.setAsync(value, duration);
    }

    @Override
    public RFuture<Void> setAndKeepTTLAsync(V value) {
        return delegate.setAndKeepTTLAsync(value);
    }

    @Override
    public RFuture<V> findCommonAsync(String name) {
        return delegate.findCommonAsync(name);
    }

    @Override
    public RFuture<Long> findCommonLengthAsync(String name) {
        return delegate.findCommonLengthAsync(name);
    }
}
