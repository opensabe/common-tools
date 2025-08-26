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
package io.github.opensabe.common.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.common.jfr.JFRObservationHandler;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.core.instrument.observation.MeterObservationHandler;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingAwareMeterObservationHandler;

@Configuration(proxyBeanMethods = false)
public class MicroMeterCustomizedConfiguration {
    @Bean
    public JFRObservationHandler jfrTracingObservationHandler(List<ObservationToJFRGenerator<? extends Observation.Context>> generators) {
        return new JFRObservationHandler(generators);
    }

    @Bean
    public UnifiedObservationFactory unifiedObservationFactory(ObjectProvider<ObservationRegistry> observationRegistry) {
        return new UnifiedObservationFactory(observationRegistry);
    }

    @Bean
    TracingAwareMeterObservationHandler<Observation.Context> tracingAwareMeterObservationHandler(
            MeterRegistry meterRegistry, Tracer tracer) {
        /**
         * @see DefaultMeterObservationHandler
         * 去掉非常消耗 CPU 的 LongTaskTimer
         */
        MeterObservationHandler<Observation.Context> delegate = new MeterObservationHandler<>() {
            @Override
            public void onStart(Observation.Context context) {

                Timer.Sample sample = Timer.start(meterRegistry);
                context.put(Timer.Sample.class, sample);
            }

            @Override
            public void onStop(Observation.Context context) {
                List<Tag> tags = createTags(context);
                tags.add(Tag.of("error", getErrorValue(context)));
                Timer.Sample sample = context.getRequired(Timer.Sample.class);
                sample.stop(Timer.builder(context.getName()).tags(tags).register(meterRegistry));
            }

            @Override
            public void onEvent(Observation.Event event, Observation.Context context) {
                Counter.builder(context.getName() + "." + event.getName())
                        .tags(createTags(context))
                        .register(meterRegistry)
                        .increment();
            }

            private String getErrorValue(Observation.Context context) {
                Throwable error = context.getError();
                return error != null ? error.getClass().getSimpleName() : "none";
            }

            private List<Tag> createTags(Observation.Context context) {
                List<Tag> tags = new ArrayList<>();
                for (KeyValue keyValue : context.getLowCardinalityKeyValues()) {
                    tags.add(Tag.of(keyValue.getKey(), keyValue.getValue()));
                }
                return tags;
            }
        };
        return new TracingAwareMeterObservationHandler<>(delegate, tracer);
    }
}
