package io.github.opensabe.spring.boot.starter.socketio.tracing.observation;

import io.micrometer.observation.ObservationConvention;

public interface ObservationCov {
    ObservationConvention getConvention();
}
