package io.github.opensabe.spring.cloud.parent.webflux.common;

import io.micrometer.observation.Observation;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

public class TracedFlux<T> extends Flux<T> {
    private final Flux<T> delegate;
    private final Observation observation;

    TracedFlux(Flux<T> delegate, Observation observation) {
        this.delegate = delegate;
        this.observation = observation;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        delegate.subscribe(new TracedCoreSubscriber(actual, observation));
    }
}
