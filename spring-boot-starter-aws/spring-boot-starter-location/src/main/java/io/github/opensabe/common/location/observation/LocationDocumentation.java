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
package io.github.opensabe.common.location.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * GeoPlaces 地理编码服务 Observation 指标与标签文档。
 */
public enum LocationDocumentation implements ObservationDocumentation {
    LOCATION {
        /** {@inheritDoc} */
        @Override
        public String getName() {
            return "location.geocode";
        }

        /** {@inheritDoc} */
        @Override
        public Class<? extends LocationConvention> getDefaultConvention() {
            return LocationConvention.class;
        }
    };

    public enum LocationTag implements KeyName {
        METHOD_NAME {
            /** {@inheritDoc} */
            @Override
            public String asString() {
                return "location.method.name";
            }
        },
        REQUEST_PARAMS {
            /** {@inheritDoc} */
            @Override
            public String asString() {
                return "location.request.params";
            }
        },
        RESPONSE {
            /** {@inheritDoc} */
            @Override
            public String asString() {
                return "location.response";
            }
        },
        EXECUTION_TIME {
            /** {@inheritDoc} */
            @Override
            public String asString() {
                return "location.execution.time";
            }
        },
        LOCATION_SUCCESSFULLY {
            /** {@inheritDoc} */
            @Override
            public String asString() {
                return "location.successfully";
            }
        },
        THROWABLE {
            /** {@inheritDoc} */
            @Override
            public String asString() {
                return "location.throwable";
            }
        };


    }
}
