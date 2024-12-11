package io.github.opensabe.common.elasticsearch.observation;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElasticSearchClientObservationContext extends Observation.Context {
    private final String uri;
    private final String params;

    private String response = "";
    private Throwable throwable;

    public ElasticSearchClientObservationContext(String uri, String params) {
        this.uri = uri;
        this.params = params;
    }
}
