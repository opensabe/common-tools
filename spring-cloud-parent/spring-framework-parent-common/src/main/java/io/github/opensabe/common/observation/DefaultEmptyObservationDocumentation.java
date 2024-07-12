package io.github.opensabe.common.observation;

import io.micrometer.observation.docs.ObservationDocumentation;

public enum DefaultEmptyObservationDocumentation implements ObservationDocumentation {
    EMPTY_OBSERVATION_DOCUMENTATION {
        @Override
        public String getName() {
            return "opensabe.default-empty";
        }
    }
    ;
}
