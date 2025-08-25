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

