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

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.micrometer.observation.Observation;

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
