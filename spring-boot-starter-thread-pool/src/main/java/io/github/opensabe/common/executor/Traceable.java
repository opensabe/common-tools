package io.github.opensabe.common.executor;


import io.micrometer.observation.Observation;
import jakarta.validation.constraints.NotNull;

public interface Traceable<V>{

    @NotNull
    Observation getObservation();

    default V trace() {
        return getObservation().scoped(this::inTrace);
    }

    V inTrace();

}
