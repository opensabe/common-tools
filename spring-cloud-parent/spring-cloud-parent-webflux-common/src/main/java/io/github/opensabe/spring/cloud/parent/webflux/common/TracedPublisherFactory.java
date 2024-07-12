package io.github.opensabe.spring.cloud.parent.webflux.common;

import io.micrometer.observation.Observation;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 起始 Flux/Mono，转折或者异步 Flux/Mono，都需要使用这个工厂创建才能保证有 TraceId
 */
@Log4j2
@AllArgsConstructor
public class TracedPublisherFactory {

    public <T> Flux<T> getTracedFlux(Flux<T> publisher, Observation observation) {
        return new TracedFlux<>(publisher, observation);
    }

    public <T> Mono<T> getTracedMono(Mono<T> publisher, Observation observation) {
        return new TracedMono<>(publisher, observation);
    }
}
