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
