package io.github.opensabe.spring.cloud.parent.webflux.common;

import io.micrometer.observation.Observation;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class TracedCoreSubscriber<T> implements Subscriber<T> {
    private final Subscriber<T> delegate;
    private final Observation observation;

    TracedCoreSubscriber(Subscriber<T> delegate, Observation observation) {
        this.delegate = delegate;
        this.observation = observation;
    }

    @Override
    public void onSubscribe(Subscription s) {
        observation.scoped(() -> {
            delegate.onSubscribe(s);
        });
    }

    @Override
    public void onError(Throwable t) {
        observation.scoped(() -> {
            delegate.onError(t);
        });
    }

    @Override
    public void onComplete() {
        observation.scoped(() -> {
            delegate.onComplete();
        });
    }

    @Override
    public void onNext(T o) {
        observation.scoped(() -> {
            delegate.onNext(o);
        });
    }
}
