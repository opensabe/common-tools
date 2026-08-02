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

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * Elasticsearch 客户端 Observation 指标与标签文档。
 */
public enum ElasticSearchClientObservationDocumentation implements ObservationDocumentation {
    CLIENT_REQUEST {
        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "elastic.search.client.request";
        }

        /** {@inheritDoc} */
        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return ElasticSearchClientConvention.class;
        }
    },
    ;

    public enum ClientRequestTag implements KeyName {
        URI {
            /** {@inheritDoc} */
            @Override
            public String asString() {
                return "elastic.search.client.request.uri";
            }
        },
        PARAMS {
            /** {@inheritDoc} */
            @Override
            public String asString() {
                return "elastic.search.client.request.params";
            }
        },
        RESPONSE {
            /** {@inheritDoc} */
            @Override
            public String asString() {
                return "elastic.search.client.response";
            }
        },
        THROWABLE {
            /** {@inheritDoc} */
            @Override
            public String asString() {
                return "elastic.search.client.throwable";
            }
        },
    }
}
