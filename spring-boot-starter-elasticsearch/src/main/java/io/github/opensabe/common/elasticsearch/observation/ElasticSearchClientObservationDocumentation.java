package io.github.opensabe.common.elasticsearch.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum ElasticSearchClientObservationDocumentation implements ObservationDocumentation {
    CLIENT_REQUEST {
        @Override
        public String getName() {
            return "elastic.search.client.request";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return ElasticSearchClientConvention.class;
        }
    },
    ;

    public enum CLIENT_REQUEST_TAG implements KeyName {
        URI {
            @Override
            public String asString() {
                return "elastic.search.client.request.uri";
            }
        },
        PARAMS {
            @Override
            public String asString() {
                return "elastic.search.client.request.params";
            }
        },
        RESPONSE {
            @Override
            public String asString() {
                return "elastic.search.client.response";
            }
        },
        THROWABLE {
            @Override
            public String asString() {
                return "elastic.search.client.throwable";
            }
        },
    }
}
