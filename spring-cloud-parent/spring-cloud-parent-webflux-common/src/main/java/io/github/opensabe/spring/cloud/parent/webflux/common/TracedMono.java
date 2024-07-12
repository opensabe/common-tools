package io.github.opensabe.spring.cloud.parent.webflux.common;


import io.micrometer.observation.Observation;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

public class TracedMono<T> extends Mono<T> {
    private final Mono<T> delegate;
    private final Observation observation;

    public TracedMono(Mono<T> delegate, Observation observation) {
        this.delegate = delegate;
        this.observation = observation;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        delegate.subscribe(new TracedCoreSubscriber(actual, observation));
    }
}

