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
