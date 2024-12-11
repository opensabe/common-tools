package io.github.opensabe.common.elasticsearch.observation;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class ElasticSearchClientConvention implements ObservationConvention<ElasticSearchClientObservationContext> {
    public static final ElasticSearchClientConvention DEFAULT = new ElasticSearchClientConvention();
    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof ElasticSearchClientObservationContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(ElasticSearchClientObservationContext context) {
        return KeyValues.empty();
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(ElasticSearchClientObservationContext context) {
        return KeyValues.of(
                ElasticSearchClientObservationDocumentation.CLIENT_REQUEST_TAG.URI.withValue(context.getUri()),
                ElasticSearchClientObservationDocumentation.CLIENT_REQUEST_TAG.PARAMS.withValue(context.getParams()),
                ElasticSearchClientObservationDocumentation.CLIENT_REQUEST_TAG.RESPONSE.withValue(context.getResponse()),
                ElasticSearchClientObservationDocumentation.CLIENT_REQUEST_TAG.THROWABLE.withValue(context.getThrowable() != null ? context.getThrowable().getMessage() : "")
        );
    }
}
